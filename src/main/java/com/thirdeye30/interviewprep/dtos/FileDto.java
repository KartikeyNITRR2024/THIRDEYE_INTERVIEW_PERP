package com.thirdeye30.interviewprep.dtos;
import com.thirdeye30.interviewprep.enums.AccessType;
import com.thirdeye30.interviewprep.enums.FileType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileDto extends ExplorerDto {
   private String url;
   private FileType fileType;
   private Long sizeInBytes;
   private AccessType accessType;
   private String creatorName;
   private String description;
}
