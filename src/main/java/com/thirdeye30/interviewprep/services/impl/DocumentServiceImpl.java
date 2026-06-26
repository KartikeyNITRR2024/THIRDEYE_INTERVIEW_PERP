package com.thirdeye30.interviewprep.services.impl;

import com.thirdeye30.interviewprep.services.DocumentService;
import com.thirdeye30.interviewprep.dtos.DocumentDto;
import com.thirdeye30.interviewprep.dtos.DownloadDocumentDto;
import com.thirdeye30.interviewprep.entities.Document;
import com.thirdeye30.interviewprep.enums.DocumentType;
import com.thirdeye30.interviewprep.repositories.DocumentRepository;
import com.thirdeye30.interviewprep.utils.CryptoUtils;
import com.thirdeye30.interviewprep.utils.DocumentCacheWrapper;
import com.thirdeye30.interviewprep.utils.Mapper;

import io.awspring.cloud.s3.S3Template;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final S3Template s3Template;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${thirdeye.bucket.name}")
    private String bucketName;

    @Value("${thirdeye.redis.document.enabled:false}")
    private boolean isRedisDocumentCacheEnabled;

    private final String redisDocumentPrefix = "doc-service:document:";

    @Override
    @Transactional
    public DocumentDto uploadDocument(MultipartFile file, DocumentType documentType, String name) {
        log.info("Initiating secure document upload for: {}", name);
        UUID documentId = UUID.randomUUID();
        String encKey = UUID.randomUUID().toString().substring(0, 8) + "-DOC-KEY";
        String extension = Mapper.getExtension(documentType);
        String s3Key = documentId.toString() + extension;

        try {
            byte[] encryptedData = CryptoUtils.encryptDocument(file.getBytes(), encKey);
            s3Template.upload(bucketName, s3Key, new java.io.ByteArrayInputStream(encryptedData));
            log.info("Successfully uploaded encrypted file to S3: {}", s3Key);
            Document document = Document.builder()
                    .uuid(documentId)
                    .name(name)
                    .documentType(documentType)
                    .size(file.getSize())
                    .encryptKey(encKey)
                    .build();
            Document savedDocument = documentRepository.save(document);
            return Mapper.mapToDto(savedDocument);
        } catch (Exception e) {
            log.error("Document Upload failed for: {}", name, e);
            throw new RuntimeException("Secure Document Upload Failed", e);
        }
    }

    @Override
    public DownloadDocumentDto downloadDocument(UUID documentId) {
        log.info("Request to download and decrypt document: {}", documentId);
        String cacheKey = redisDocumentPrefix + documentId;
        if (isRedisDocumentCacheEnabled) {
            try {
                DocumentCacheWrapper cached = (DocumentCacheWrapper) redisTemplate.opsForValue().get(cacheKey);
                if (cached != null) {
                    log.info("Cache hit for document: {}", documentId);
                    DownloadDocumentDto cachedDto = new DownloadDocumentDto();
                    cachedDto.setBytes(java.util.Base64.getDecoder().decode(cached.getBase64Bytes()));
                    cachedDto.setContentType(MediaType.parseMediaType(cached.getContentType()));
                    cachedDto.setFileName(cached.getFileName());
                    return cachedDto;
                }
            } catch (Exception e) {
                log.error("Redis read failed during document cache check for: {}", documentId, e);
            }
        }

        log.info("Cache miss or caching disabled for document: {}. Fetching from DB and S3.", documentId);
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found with ID: " + documentId));

        String extension = Mapper.getExtension(document.getDocumentType());
        MediaType contentType = Mapper.getMediaType(document.getDocumentType());
        String fileName = document.getName() + extension;
        String s3Key = document.getUuid().toString() + extension;
        
        try {
            Resource resource = s3Template.download(bucketName, s3Key);
            byte[] encryptedData = StreamUtils.copyToByteArray(resource.getInputStream());
            byte[] decryptedData = CryptoUtils.decryptDocument(encryptedData, document.getEncryptKey());

            DownloadDocumentDto downloadDto = DownloadDocumentDto.builder()
                    .bytes(decryptedData)
                    .contentType(contentType)
                    .fileName(fileName)
                    .build();
            
            // --- UPDATED CACHE WRITE LOGIC ---
            if (isRedisDocumentCacheEnabled) {
                try {
                    DocumentCacheWrapper wrapper = new DocumentCacheWrapper();
                    wrapper.setBase64Bytes(java.util.Base64.getEncoder().encodeToString(decryptedData));
                    wrapper.setContentType(contentType.toString());
                    wrapper.setFileName(fileName);
                    
                    redisTemplate.opsForValue().set(cacheKey, wrapper, Duration.ofMinutes(15));
                    log.info("Successfully cached document details in Redis for key: {}", documentId);
                } catch (Exception cacheEx) {
                    log.error("Failed to write document cache to Redis for key: {}", documentId, cacheEx);
                }
            }
            
            return downloadDto;
        } catch (Exception e) {
            log.error("Download/Decryption failed for document: {}", documentId, e);
            throw new RuntimeException("Failed to process document: " + e.getMessage());
        }
    }

    
}