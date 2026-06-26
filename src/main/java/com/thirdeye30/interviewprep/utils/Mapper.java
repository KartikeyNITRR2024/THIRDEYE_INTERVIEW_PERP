package com.thirdeye30.interviewprep.utils;

import org.springframework.http.MediaType;

import com.thirdeye30.interviewprep.dtos.DocumentDto;
import com.thirdeye30.interviewprep.dtos.FileDto;
import com.thirdeye30.interviewprep.dtos.FileUploadDto;
import com.thirdeye30.interviewprep.dtos.FolderDto;
import com.thirdeye30.interviewprep.entities.Document;
import com.thirdeye30.interviewprep.entities.File;
import com.thirdeye30.interviewprep.entities.Folder;
import com.thirdeye30.interviewprep.enums.DocumentType;

public class Mapper {
	public static Folder mapToEntity(FolderDto dto) {
        Folder folder = new Folder();
        folder.setName(dto.getName());
        folder.setParentUuid(dto.getParentUuid());
        folder.setNoOfFiles(dto.getNoOfFiles() != null ? dto.getNoOfFiles() : 0);
        folder.setNoOfFolders(dto.getNoOfFolders() != null ? dto.getNoOfFolders() : 0);
        folder.setViewCount(dto.getViewCount() != null ? dto.getViewCount() : 0L);
        folder.setDownloadCount(dto.getDownloadCount() != null ? dto.getDownloadCount() : 0L);
        return folder;
    }

	public static FolderDto mapToDto(Folder entity) {
        FolderDto dto = new FolderDto();
        dto.setUuid(entity.getUuid());
        dto.setType(entity.getType());
        dto.setParentUuid(entity.getParentUuid());
        dto.setName(entity.getName());
        dto.setCreatedTime(entity.getCreatedTime());
        dto.setNoOfFiles(entity.getNoOfFiles());
        dto.setNoOfFolders(entity.getNoOfFolders());
        dto.setViewCount(entity.getViewCount());
        dto.setDownloadCount(entity.getDownloadCount());
        return dto;
    }
	
	public static File mapToEntity(FileUploadDto dto) {
        File file = new File();
        file.setName(dto.getName());
        file.setParentUuid(dto.getParentUuid());
        if(dto.getIsInternalDocumentId())
        {
        	file.setInternalDocumentId(dto.getInternalDocumentId());
        }
        else
        {
            file.setExternalUrl(dto.getExternalUrl());
        }
        file.setFileType(dto.getFileType());
        file.setSizeInBytes(dto.getSizeInBytes());
        file.setAccessType(dto.getAccessType());
        file.setCreatorName(dto.getCreatorName());
        file.setDescription(dto.getDescription());
        file.setViewCount(dto.getViewCount() != null ? dto.getViewCount() : 0L);
        file.setDownloadCount(dto.getDownloadCount() != null ? dto.getDownloadCount() : 0L);
        return file;
    }

	public static FileDto mapToDto(File entity, String createdUrl) {
        FileDto dto = new FileDto();
        dto.setUuid(entity.getUuid());
        dto.setType(entity.getType());
        dto.setParentUuid(entity.getParentUuid());
        dto.setName(entity.getName());
        dto.setCreatedTime(entity.getCreatedTime());
        if(entity.getInternalDocumentId() != null && entity.getInternalDocumentId().toString().length() > 0)
        {
        	dto.setUrl(createdUrl);
        }
        else
        {
            dto.setUrl(entity.getExternalUrl());
        }
        dto.setFileType(entity.getFileType());
        dto.setSizeInBytes(entity.getSizeInBytes());
        dto.setAccessType(entity.getAccessType());
        dto.setCreatorName(entity.getCreatorName());
        dto.setDescription(entity.getDescription());
        dto.setViewCount(entity.getViewCount());
        dto.setDownloadCount(entity.getDownloadCount());
        return dto;
    }
	
	public static DocumentDto mapToDto(Document entity) {
	       return DocumentDto.builder()
	                .uuid(entity.getUuid())
	                .name(entity.getName())
	                .documentType(entity.getDocumentType())
	                .size(entity.getSize())
	                .encryptKey(entity.getEncryptKey())
	                .build();
	 }

	public static String getExtension(DocumentType type) {
	    if (type == null) return ".bin";
	        return switch (type.name()) {
	            case "PDF" -> ".pdf";
	            case "IMAGE" -> ".jpg";
	            case "TEXT" -> ".txt";
	            case "DOCUMENT" -> ".docx";
	            default -> ".bin";
	        };
	}

	public static MediaType getMediaType(DocumentType type) {
	    if (type == null) return MediaType.APPLICATION_OCTET_STREAM;
	       return switch (type.name()) {
	          case "PDF" -> MediaType.APPLICATION_PDF;
	          case "IMAGE" -> MediaType.IMAGE_JPEG;
	          case "TEXT" -> MediaType.TEXT_PLAIN;
	          case "DOCUMENT" -> MediaType.valueOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
	          default -> MediaType.APPLICATION_OCTET_STREAM;
	    };
	}

}
