package com.thirdeye30.interviewprep.utils;

public class DocumentCacheWrapper {
    private String base64Bytes;
    private String contentType;
    private String fileName;

    public DocumentCacheWrapper() {}

    public String getBase64Bytes() { return base64Bytes; }
    public void setBase64Bytes(String base64Bytes) { this.base64Bytes = base64Bytes; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}
