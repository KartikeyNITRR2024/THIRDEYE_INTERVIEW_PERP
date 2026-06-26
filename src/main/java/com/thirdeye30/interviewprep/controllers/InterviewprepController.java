package com.thirdeye30.interviewprep.controllers;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thirdeye30.interviewprep.dtos.DownloadDocumentDto;
import com.thirdeye30.interviewprep.dtos.ExplorerDto;
import com.thirdeye30.interviewprep.enums.ActionType;
import com.thirdeye30.interviewprep.enums.Type;
import com.thirdeye30.interviewprep.services.DocumentService;
import com.thirdeye30.interviewprep.services.ExplorerService;
import com.thirdeye30.interviewprep.services.FolderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/interviewperp")
@RequiredArgsConstructor
public class InterviewprepController {
    
    private final FolderService folderService;
    private final ExplorerService explorerService;
    private final DocumentService documentService;
    
    @GetMapping("/root")
    public ResponseEntity<Page<ExplorerDto>> getRootContents(
            @RequestParam(required = false) Type typeFilter,
            Pageable pageable) {
        return ResponseEntity.ok(folderService.getRootContents(typeFilter, pageable));
    }
    
    @GetMapping("/contents/{id}")
    public ResponseEntity<Page<ExplorerDto>> getFolderContents(
            @PathVariable UUID id,
            @RequestParam(required = false) Type typeFilter,
            Pageable pageable) {
        return ResponseEntity.ok(folderService.getFolderContents(id, typeFilter, pageable));
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<ExplorerDto>> globalSearch(
            @RequestParam String query,
            Pageable pageable) {
        return ResponseEntity.ok(explorerService.searchByName(query, pageable));
    }
    
    @GetMapping("/file/{documentId}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable UUID documentId) {
        return buildFileResponse(documentId, "attachment");
    }

    @GetMapping("/file/{documentId}/view")
    public ResponseEntity<byte[]> viewDocument(@PathVariable UUID documentId) {
        return buildFileResponse(documentId, "inline");
    }

    private ResponseEntity<byte[]> buildFileResponse(UUID documentId, String dispositionType) {
        DownloadDocumentDto downloadDto = documentService.downloadDocument(documentId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(downloadDto.getContentType());
        
        ContentDisposition contentDisposition = ContentDisposition.builder(dispositionType)
                .filename(downloadDto.getFileName())
                .build();
                
        headers.setContentDisposition(contentDisposition);

        return new ResponseEntity<>(downloadDto.getBytes(), headers, HttpStatus.OK);
    }
    
    @PostMapping("/action/{id}")
    public ResponseEntity<Void> recordAction(
            @PathVariable UUID id,
            @RequestParam ActionType type) {
        explorerService.recordAction(id, type);
        return ResponseEntity.accepted().build(); 
    }
}