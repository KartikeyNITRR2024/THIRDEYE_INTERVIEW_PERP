package com.thirdeye30.interviewprep.dtos;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.thirdeye30.interviewprep.enums.Status;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PriorityDto {
	private UUID id;
	private Status status;
	private Map<String, List<List<String>>> coursePath;
}
