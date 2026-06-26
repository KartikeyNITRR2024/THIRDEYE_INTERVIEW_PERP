package com.thirdeye30.interviewprep.controllers;

import com.thirdeye30.interviewprep.dtos.ExplorerDto;
import com.thirdeye30.interviewprep.dtos.FolderDto;
import com.thirdeye30.interviewprep.enums.Type;
import com.thirdeye30.interviewprep.services.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/interviewperp/admin/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @PostMapping
    public ResponseEntity<FolderDto> createFolder(@RequestBody FolderDto folderDto) {
        return new ResponseEntity<>(folderService.createFolder(folderDto), HttpStatus.CREATED);
    }

    @GetMapping("/content/{id}")
    public ResponseEntity<FolderDto> getFolderById(@PathVariable UUID id) {
        return ResponseEntity.ok(folderService.getFolderById(id));
    }

    @GetMapping
    public ResponseEntity<List<FolderDto>> getAllFolders() {
        return ResponseEntity.ok(folderService.getAllFolders());
    }

    @PutMapping("/{id}")
    public ResponseEntity<FolderDto> updateFolder(@PathVariable UUID id, @RequestBody FolderDto folderDto) {
        return ResponseEntity.ok(folderService.updateFolder(id, folderDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFolder(@PathVariable UUID id) {
        folderService.deleteFolder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/contents/{id}")
    public ResponseEntity<Page<ExplorerDto>> getFolderContents(
            @PathVariable UUID id,
            @RequestParam(required = false) Type typeFilter,
            Pageable pageable) {
        return ResponseEntity.ok(folderService.getFolderContents(id, typeFilter, pageable));
    }
    
    @GetMapping("/root")
    public ResponseEntity<Page<ExplorerDto>> getRootContents(
            @RequestParam(required = false) Type typeFilter,
            Pageable pageable) {
        return ResponseEntity.ok(folderService.getRootContents(typeFilter, pageable));
    }
}
