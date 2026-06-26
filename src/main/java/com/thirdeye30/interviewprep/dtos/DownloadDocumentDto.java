package com.thirdeye30.interviewprep.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DownloadDocumentDto {
    private byte[] bytes;
    private MediaType contentType;
    private String fileName;
}
