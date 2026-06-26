package com.thirdeye30.interviewprep.utils;

import java.util.List;

import com.thirdeye30.interviewprep.dtos.ExplorerDto;

public class PageCacheWrapper {
    private List<ExplorerDto> content;
    private long totalElements;

    public PageCacheWrapper() {
    }

    public PageCacheWrapper(List<ExplorerDto> content, long totalElements) {
        this.content = content;
        this.totalElements = totalElements;
    }

    public List<ExplorerDto> getContent() {
        return content;
    }

    public void setContent(List<ExplorerDto> content) {
        this.content = content;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }
}
