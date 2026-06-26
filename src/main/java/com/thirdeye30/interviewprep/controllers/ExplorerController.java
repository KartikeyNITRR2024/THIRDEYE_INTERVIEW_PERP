package com.thirdeye30.interviewprep.controllers;

import com.thirdeye30.interviewprep.dtos.ExplorerDto;
import com.thirdeye30.interviewprep.enums.ActionType;
import com.thirdeye30.interviewprep.services.ExplorerService;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/interviewperp/admin/explorer")
@RequiredArgsConstructor
public class ExplorerController {

    private final ExplorerService explorerService;

    @GetMapping("/search")
    public ResponseEntity<Page<ExplorerDto>> globalSearch(
            @RequestParam String query,
            Pageable pageable) {
        return ResponseEntity.ok(explorerService.searchByName(query, pageable));
    }
    
    @PostMapping("/action/{id}")
    public ResponseEntity<Void> recordAction(
            @PathVariable UUID id,
            @RequestParam ActionType type) {
        explorerService.recordAction(id, type);
        return ResponseEntity.accepted().build(); 
    }
}
