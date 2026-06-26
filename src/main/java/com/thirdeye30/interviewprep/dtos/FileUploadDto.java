package com.thirdeye30.interviewprep.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

import com.thirdeye30.interviewprep.enums.AccessType;
import com.thirdeye30.interviewprep.enums.FileType;
import com.thirdeye30.interviewprep.enums.Type;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadDto extends ExplorerDto {
	   private String externalUrl;
	   private UUID internalDocumentId;
	   private Boolean isInternalDocumentId;
	   private FileType fileType;
	   private Long sizeInBytes;
	   private AccessType accessType;
	   private String creatorName;
	   private String description;
}
