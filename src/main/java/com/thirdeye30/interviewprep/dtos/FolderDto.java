package com.thirdeye30.interviewprep.dtos;
import java.util.Map;

import com.thirdeye30.interviewprep.entities.File;
import com.thirdeye30.interviewprep.enums.FileType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FolderDto extends ExplorerDto {
	private Integer noOfFiles;
	private Integer noOfFolders;
	private Map<String, String> path;
}
