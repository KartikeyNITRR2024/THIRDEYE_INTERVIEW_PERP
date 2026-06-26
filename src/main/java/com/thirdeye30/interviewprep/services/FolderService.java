package com.thirdeye30.interviewprep.services;

import com.thirdeye30.interviewprep.dtos.ExplorerDto;
import com.thirdeye30.interviewprep.dtos.FolderDto;
import com.thirdeye30.interviewprep.enums.Type;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FolderService {
    FolderDto createFolder(FolderDto folderDto);
    FolderDto getFolderById(UUID id);
    List<FolderDto> getAllFolders();
    FolderDto updateFolder(UUID id, FolderDto folderDto);
    void deleteFolder(UUID id);
	Page<ExplorerDto> getFolderContents(UUID parentUuid, Type typeFilter, Pageable pageable);
	List<ExplorerDto> getFolderContents(UUID parentUuid);
	Page<ExplorerDto> getRootContents(Type typeFilter, Pageable pageable);
	void evictParentPageCache(UUID parentUuid);
}