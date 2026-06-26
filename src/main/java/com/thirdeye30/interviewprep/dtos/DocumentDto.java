package com.thirdeye30.interviewprep.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

import com.thirdeye30.interviewprep.enums.DocumentType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentDto {
	private UUID uuid;
	private String name;
	private DocumentType documentType;
	private Long size;
	private String encryptKey;
}
