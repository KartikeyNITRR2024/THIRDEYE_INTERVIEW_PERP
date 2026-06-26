package com.thirdeye30.interviewprep.services.impl;

import com.thirdeye30.interviewprep.dtos.FileDto;
import com.thirdeye30.interviewprep.dtos.FileUploadDto;
import com.thirdeye30.interviewprep.entities.File;
import com.thirdeye30.interviewprep.enums.ActionType;
import com.thirdeye30.interviewprep.repositories.FileRepository;
import com.thirdeye30.interviewprep.services.ExplorerService;
import com.thirdeye30.interviewprep.services.FileService;
import com.thirdeye30.interviewprep.services.FolderService;
import com.thirdeye30.interviewprep.utils.Mapper;
import com.thirdeye30.interviewprep.utils.Utils;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final ExplorerService explorerService;
    private final FolderService folderService;
    private final Utils utils;
    
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${thirdeye.redis.response.enabled:false}")
    private boolean isRedisResponseCacheEnabled;

    private final String redisFilePrefix = "file-service:file:";
    private final Duration cacheDuration = Duration.ofMinutes(30);

    @Override
    @Transactional
    public FileDto createFile(FileUploadDto fileDto) {
        File file = Mapper.mapToEntity(fileDto);
        File savedFile = fileRepository.save(file);
        explorerService.recordAction(fileDto.getParentUuid(), ActionType.ADD_FILE);
        
        FileDto dto = Mapper.mapToDto(savedFile, savedFile.getInternalDocumentId() != null 
                ? utils.documentUrlGenerator(savedFile.getInternalDocumentId()) 
                : savedFile.getExternalUrl());

        if (isRedisResponseCacheEnabled) {
            try {
                redisTemplate.opsForValue().set(redisFilePrefix + savedFile.getUuid(), dto, cacheDuration);
            } catch (Exception e) {
                log.error("Redis write failed during file creation for: {}", savedFile.getUuid(), e);
            }
        }
        
        folderService.evictParentPageCache(dto.getParentUuid());
        
        return dto;
    }

    @Override
    public FileDto getFileById(UUID id) {
        String cacheKey = redisFilePrefix + id;

        if (isRedisResponseCacheEnabled) {
            try {
                FileDto cachedDto = (FileDto) redisTemplate.opsForValue().get(cacheKey);
                if (cachedDto != null) {
                    return cachedDto;
                }
            } catch (Exception e) {
                log.error("Redis read failed during file cache check for: {}", id, e);
            }
        }

        File file = fileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("File not found with ID: " + id));
                
        FileDto dto = Mapper.mapToDto(file, file.getInternalDocumentId() != null 
                ? utils.documentUrlGenerator(file.getInternalDocumentId()) 
                : file.getExternalUrl());

        if (isRedisResponseCacheEnabled) {
            try {
                redisTemplate.opsForValue().set(cacheKey, dto, cacheDuration);
            } catch (Exception e) {
                log.error("Failed to write file cache to Redis for key: {}", id, e);
            }
        }

        return dto;
    }

    @Override
    public List<FileDto> getAllFiles() {
        return fileRepository.findAll()
                .stream()
                .map(file -> Mapper.mapToDto(file, file.getInternalDocumentId() != null 
                        ? utils.documentUrlGenerator(file.getInternalDocumentId()) 
                        : file.getExternalUrl()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FileDto updateFile(UUID id, FileUploadDto fileDto) {
        File existingFile = fileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("File not found with ID: " + id));

        UUID oldParentUuid = existingFile.getParentUuid();

        existingFile.setName(fileDto.getName());
        existingFile.setParentUuid(fileDto.getParentUuid());
        
        if (fileDto.getIsInternalDocumentId()) {
            existingFile.setInternalDocumentId(fileDto.getInternalDocumentId());
        } else {
            existingFile.setExternalUrl(fileDto.getExternalUrl());
        }
        
        existingFile.setFileType(fileDto.getFileType());
        existingFile.setSizeInBytes(fileDto.getSizeInBytes());
        existingFile.setAccessType(fileDto.getAccessType());
        existingFile.setCreatorName(fileDto.getCreatorName());
        existingFile.setDescription(fileDto.getDescription());

        File updatedFile = fileRepository.save(existingFile);
        FileDto dto = Mapper.mapToDto(updatedFile, updatedFile.getInternalDocumentId() != null 
                ? utils.documentUrlGenerator(updatedFile.getInternalDocumentId()) 
                : updatedFile.getExternalUrl());

        if (isRedisResponseCacheEnabled) {
            try {
                redisTemplate.opsForValue().set(redisFilePrefix + id, dto, cacheDuration);
            } catch (Exception e) {
                log.error("Redis update failed for file: {}", id, e);
            }
        }

        if (oldParentUuid != null && !oldParentUuid.equals(dto.getParentUuid())) {
            folderService.evictParentPageCache(oldParentUuid);
        }
        folderService.evictParentPageCache(dto.getParentUuid());
        
        return dto;
    }

    @Override
    @Transactional
    public void deleteFile(UUID id) {
        File existingFile = fileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("File not found with ID: " + id));
        
        UUID parentUuid = existingFile.getParentUuid();
        fileRepository.deleteById(id);

        if (isRedisResponseCacheEnabled) {
            try {
                redisTemplate.delete(redisFilePrefix + id);
            } catch (Exception e) {
                log.error("Redis eviction failed for file: {}", id, e);
            }
        }

        folderService.evictParentPageCache(parentUuid);
    }
}