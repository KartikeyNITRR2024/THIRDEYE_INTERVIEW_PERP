package com.thirdeye30.interviewprep.services;

import java.util.List;

import com.thirdeye30.interviewprep.dtos.CoursePayload;
import com.thirdeye30.interviewprep.dtos.Message;

public interface MessageBrokerService {
	List<Message<CoursePayload>> getPayloadMessage(String topicName);
	void sendMultipleMessages(String topicName, Object messages);
}

