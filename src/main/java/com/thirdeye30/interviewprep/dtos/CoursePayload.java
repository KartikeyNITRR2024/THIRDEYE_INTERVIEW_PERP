package com.thirdeye30.interviewprep.dtos;

import java.util.List;
import java.util.UUID;
import com.thirdeye30.interviewprep.enums.MailType;
import com.thirdeye30.interviewprep.enums.Status;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class CoursePayload {
	private UUID id;
	private List<String> highPriority;
	private List<String> mediumPriority;
	private List<String> lowPriority;
	private String company;
}
