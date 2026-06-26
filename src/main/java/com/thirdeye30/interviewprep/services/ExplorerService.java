package com.thirdeye30.interviewprep.services;

import com.thirdeye30.interviewprep.dtos.ExplorerDto;
import com.thirdeye30.interviewprep.enums.ActionType;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExplorerService {
    Page<ExplorerDto> searchByName(String query, Pageable pageable);

	void recordAction(UUID id, ActionType actionType);

	void updateActionInDatabase();
    
}