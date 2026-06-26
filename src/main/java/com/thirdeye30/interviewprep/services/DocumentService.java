package com.thirdeye30.interviewprep.services;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.thirdeye30.interviewprep.dtos.DocumentDto;
import com.thirdeye30.interviewprep.dtos.DownloadDocumentDto;
import com.thirdeye30.interviewprep.enums.DocumentType;

public interface DocumentService {

	DocumentDto uploadDocument(MultipartFile file, DocumentType documentType, String name);

	DownloadDocumentDto downloadDocument(UUID documentId);

}
