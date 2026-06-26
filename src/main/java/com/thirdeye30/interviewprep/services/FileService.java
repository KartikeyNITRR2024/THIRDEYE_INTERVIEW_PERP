package com.thirdeye30.interviewprep.services;

import com.thirdeye30.interviewprep.dtos.FileDto;
import com.thirdeye30.interviewprep.dtos.FileUploadDto;

import java.util.List;
import java.util.UUID;

public interface FileService {
    FileDto createFile(FileUploadDto fileDto);
    FileDto getFileById(UUID id);
    List<FileDto> getAllFiles();
    FileDto updateFile(UUID id, FileUploadDto fileDto);
    void deleteFile(UUID id);
}