package com.thirdeye30.interviewprep.controllers;

import com.thirdeye30.interviewprep.dtos.DocumentDto;
import com.thirdeye30.interviewprep.dtos.DownloadDocumentDto;
import com.thirdeye30.interviewprep.enums.DocumentType;
import com.thirdeye30.interviewprep.services.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/interviewperp/admin/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentDto> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") DocumentType documentType,
            @RequestParam("name") String name) {
        
        return new ResponseEntity<>(documentService.uploadDocument(file, documentType, name), HttpStatus.CREATED);
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable UUID documentId) {
        return buildFileResponse(documentId, "attachment");
    }

    @GetMapping("/{documentId}/view")
    public ResponseEntity<byte[]> viewDocument(@PathVariable UUID documentId) {
        return buildFileResponse(documentId, "inline");
    }

    private ResponseEntity<byte[]> buildFileResponse(UUID documentId, String dispositionType) {
        DownloadDocumentDto downloadDto = documentService.downloadDocument(documentId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(downloadDto.getContentType());
        headers.setContentDispositionFormData(dispositionType, downloadDto.getFileName());

        return new ResponseEntity<>(downloadDto.getBytes(), headers, HttpStatus.OK);
    }
}