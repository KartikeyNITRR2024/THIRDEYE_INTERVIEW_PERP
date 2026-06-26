package com.thirdeye30.interviewprep.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

import com.thirdeye30.interviewprep.entities.File;
import com.thirdeye30.interviewprep.enums.FileType;
import com.thirdeye30.interviewprep.enums.Type;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExplorerDto {
   private UUID uuid;
   private Type type;
   private UUID parentUuid;
   private String name;
   private Long viewCount;
   private Long downloadCount;
   private LocalDateTime createdTime;
}
