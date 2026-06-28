package com.thirdeye30.interviewprep.services.impl;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.thirdeye30.interviewprep.configs.MessageBrokerConfig;
import com.thirdeye30.interviewprep.dtos.CoursePayload;
import com.thirdeye30.interviewprep.dtos.Message;
import com.thirdeye30.interviewprep.externalcontollers.MessageBrokerClient;
import com.thirdeye30.interviewprep.services.MessageBrokerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageBrokerServiceImpl implements MessageBrokerService {
	
    @Value("${thirdeye.maximumqueries}")
    private Long queries;
    
    private final MessageBrokerConfig messageBrokerConfig; 
    private final MessageBrokerClient messageBroker;
    
    @Override
    public void sendMultipleMessages(String topicName, Object messages)
    {
    	if(!messageBrokerConfig.getTopics().containsKey(topicName))
    	{
    		throw new RuntimeException("Does not have any topic with topic name "+topicName);
    	}
    	try {
    		ResponseEntity<String> response = messageBroker.setMultipleMessages(topicName, messageBrokerConfig.getTopics().get(topicName).getTopicKey(), messages);
    		if (response.getStatusCode().equals(HttpStatus.CREATED)) {
                log.info("Successfully send messages to message broker with topic name "+topicName);
            }
    		else
    		{
    		    throw new RuntimeException("Failed to send messages to message broker with topic name "+topicName+" "+response.getBody());
    		}
    	} catch (Exception e) {
    		throw new RuntimeException("Failed to send messages to message broker with topic name "+topicName+" "+e.getMessage());
        }
    }
    
    @Override
	public List<Message<CoursePayload>> getPayloadMessage(String topicName)
	{
		List<Message<CoursePayload>> messages = new ArrayList<>();
		if(!messageBrokerConfig.getTopics().containsKey(topicName))
    	{
    		throw new RuntimeException("Does not have any topic with topic name "+topicName);
    	}
		try {
    		ResponseEntity<List<Message<CoursePayload>>> response = messageBroker.getMessages(topicName, messageBrokerConfig.getTopics().get(topicName).getTopicKey(), queries);
    		if (response.getStatusCode().equals(HttpStatus.OK)) {
    			messages = response.getBody();
                log.info("Successfully received messages from message broker with topic name "+topicName);
            }
    		else
    		{
    		    throw new RuntimeException("Failed to recive messages from message broker with topic name "+topicName+" "+response.getBody());
    		}
    	} catch (Exception e) {
    		log.info("Failed to read data");
        }
		return messages;
	}
}



