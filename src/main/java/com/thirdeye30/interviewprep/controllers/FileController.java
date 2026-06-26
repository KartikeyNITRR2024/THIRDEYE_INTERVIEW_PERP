package com.thirdeye30.interviewprep.controllers;

import com.thirdeye30.interviewprep.dtos.FileDto;
import com.thirdeye30.interviewprep.dtos.FileUploadDto;
import com.thirdeye30.interviewprep.services.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/interviewperp/admin/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping
    public ResponseEntity<FileDto> createFile(@RequestBody FileUploadDto fileDto) {
        return new ResponseEntity<>(fileService.createFile(fileDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileDto> getFileById(@PathVariable UUID id) {
        return ResponseEntity.ok(fileService.getFileById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FileDto> updateFile(@PathVariable UUID id, @RequestBody FileUploadDto fileDto) {
        return ResponseEntity.ok(fileService.updateFile(id, fileDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable UUID id) {
        fileService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }
}
