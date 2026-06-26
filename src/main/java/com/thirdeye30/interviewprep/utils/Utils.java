package com.thirdeye30.interviewprep.utils;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Utils {
	
    @Value("${thirdeye.multimedia.url.starter}")
    private String urlStarter;

    public String documentUrlGenerator(UUID documentId)
    {
       return urlStarter+"/interviewperp/file/"+documentId.toString()+"/view";
    }
}
