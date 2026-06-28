package com.thirdeye30.interviewprep.services.impl;

import com.thirdeye30.interviewprep.dtos.ActionStats;
import com.thirdeye30.interviewprep.dtos.CoursePayload;
import com.thirdeye30.interviewprep.dtos.ExplorerDto;
import com.thirdeye30.interviewprep.dtos.Message;
import com.thirdeye30.interviewprep.dtos.PriorityDto;
import com.thirdeye30.interviewprep.entities.Explorer;
import com.thirdeye30.interviewprep.entities.File;
import com.thirdeye30.interviewprep.entities.Folder;
import com.thirdeye30.interviewprep.enums.ActionType;
import com.thirdeye30.interviewprep.enums.Status;
import com.thirdeye30.interviewprep.repositories.ExplorerRepository;
import com.thirdeye30.interviewprep.services.ExplorerService;
import com.thirdeye30.interviewprep.services.MessageBrokerService;
import com.thirdeye30.interviewprep.utils.Mapper;
import com.thirdeye30.interviewprep.utils.PageCacheWrapper;
import com.thirdeye30.interviewprep.utils.Utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExplorerServiceImpl implements ExplorerService {

    private final ExplorerRepository explorerRepository;
    private final MessageBrokerService messageBrokerService;
    private final Utils utils;
    private final JdbcTemplate jdbcTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    private final AtomicReference<ConcurrentHashMap<UUID, ActionStats>> trackingMapRef = 
            new AtomicReference<>(new ConcurrentHashMap<>());

    @Value("${thirdeye.redis.response.enabled:false}")
    private boolean isRedisResponseCacheEnabled;

    private final String redisExplorerSearchPrefix = "explorer-service:search:";
    private final Duration searchCacheDuration = Duration.ofMinutes(5);

    @Override
    public Page<ExplorerDto> searchByName(String query, Pageable pageable) {
        String sortStr = pageable.getSort().isSorted() ? pageable.getSort().toString().replace(":", "-").replace(" ", "") : "UNSORTED";
        String cacheKey = String.format("%squery:%s:page:%d:size:%d:sort:%s", 
                redisExplorerSearchPrefix, query.toLowerCase(), pageable.getPageNumber(), pageable.getPageSize(), sortStr);

        if (isRedisResponseCacheEnabled) {
            try {
                PageCacheWrapper cached = (PageCacheWrapper) redisTemplate.opsForValue().get(cacheKey);
                if (cached != null) {
                    log.info("Cache hit for explorer search: {}", cacheKey);
                    return new PageImpl<>(cached.getContent(), pageable, cached.getTotalElements());
                }
            } catch (Exception e) {
                log.error("Redis read failed during search cache check for key: {}", cacheKey, e);
            }
        }

        log.info("Executing global explorer search for query: {}", query);
        Page<Explorer> searchResults = explorerRepository.findByNameStartingWithIgnoreCase(query, pageable);
        
        Page<ExplorerDto> resultPage = searchResults.map(explorer -> {
            if (explorer instanceof Folder) {
                return Mapper.mapToDto((Folder) explorer); 
            } else if (explorer instanceof File) {
                File file = (File) explorer;
                return Mapper.mapToDto(file, file.getInternalDocumentId() != null 
                        ? utils.documentUrlGenerator(file.getInternalDocumentId()) 
                        : file.getExternalUrl());
            }
            throw new RuntimeException("Unknown explorer type encountered during search");
        });

        if (isRedisResponseCacheEnabled) {
            try {
                PageCacheWrapper wrapper = new PageCacheWrapper(resultPage.getContent(), resultPage.getTotalElements());
                redisTemplate.opsForValue().set(cacheKey, wrapper, searchCacheDuration);
                log.info("Successfully cached search results for key: {}", cacheKey);
            } catch (Exception e) {
                log.error("Failed to write search cache to Redis for key: {}", cacheKey, e);
            }
        }

        return resultPage;
    }
    
    @Override
    public void recordAction(UUID id, ActionType actionType) {
        ActionStats stats = trackingMapRef.get().computeIfAbsent(id, k -> new ActionStats());
        if (ActionType.VIEW.equals(actionType)) {
            stats.views.increment();
        } else if (ActionType.DOWNLOAD.equals(actionType)) {
            stats.downloads.increment();
        } else if (ActionType.ADD_FILE.equals(actionType)) {
            stats.addFiles.increment();
        } else if (ActionType.ADD_FOLDER.equals(actionType)) {
            stats.addFolders.increment();
        }
    }
    
    @Override
    @Transactional
    public void updateActionInDatabase() {
        ConcurrentHashMap<UUID, ActionStats> mapToProcess = trackingMapRef.getAndSet(new ConcurrentHashMap<>());

        if (mapToProcess.isEmpty()) {
            log.debug("No views and downloads to update in database");
            return;
        }

        List<Map.Entry<UUID, ActionStats>> entries = new ArrayList<>(mapToProcess.entrySet());
        String sql = "UPDATE explorer SET view_count = view_count + ?, download_count = download_count + ?, no_of_files = no_of_files + ?, no_of_folders = no_of_folders + ? WHERE uuid = ?";

        try {
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Map.Entry<UUID, ActionStats> entry = entries.get(i);                
                    long views = entry.getValue().views.sum();
                    long downloads = entry.getValue().downloads.sum();
                    long newFolders = entry.getValue().addFolders.sum();
                    long newFiles = entry.getValue().addFiles.sum();
                    
                    ps.setLong(1, views);
                    ps.setLong(2, downloads);
                    ps.setLong(3, newFiles);
                    ps.setLong(4, newFolders);
                    ps.setObject(5, entry.getKey()); 
                }

                @Override
                public int getBatchSize() {
                    return entries.size();
                }
            });
            
            log.info("Successfully batched updated {} file/folder stats in DB.", entries.size());

        } catch (Exception e) {
            log.error("Database batch update failed! Merging {} records back into memory to prevent data loss.", entries.size(), e);
            
            mapToProcess.forEach((uuid, failedStats) -> {
                ActionStats currentStats = trackingMapRef.get().computeIfAbsent(uuid, k -> new ActionStats());
                currentStats.views.add(failedStats.views.sum());
                currentStats.downloads.add(failedStats.downloads.sum());
                currentStats.addFiles.add(failedStats.addFiles.sum());
                currentStats.addFolders.add(failedStats.addFolders.sum());
            });
        }
    }

	@Override
	public void createCourses() {
		List<PriorityDto> payloads = new ArrayList<>();
		while (true) {
            try {
                List<Message<CoursePayload>> messages = messageBrokerService.getPayloadMessage("courseprocesser");
                if (messages == null || messages.isEmpty()) {
                    break;
                }
                log.info("Processing {} courses", messages.size());
                for (Message<CoursePayload> message : messages) {
                    if (message != null && message.getMessage() != null) {
                    	CoursePayload coursePayload = message.getMessage();
                    	PriorityDto priorityDto = null;
                		Map<String, List<List<String>>> coursePath = new LinkedHashMap<>();
                    	try
                    	{
                    		createCoursePath(coursePath, "High Priority", coursePayload.getHighPriority());
                    		createCoursePath(coursePath, "Medium Priority", coursePayload.getMediumPriority());
                    		createCoursePath(coursePath, "Low Priority", coursePayload.getLowPriority());
                    		priorityDto = new PriorityDto(coursePayload.getId(), Status.COURSE_CREATION_COMPLETED, coursePath);
                    	} catch (Exception ex)
                    	{
                    		priorityDto = new PriorityDto(coursePayload.getId(), Status.COURSE_CREATION_FAILED, coursePath);
                    	}
                        payloads.add(priorityDto);
                    }
                }
            } catch (Exception ex) {
                log.error("Error pulling messages from the mail queue", ex);
                break;
            }
        }
		if(!payloads.isEmpty())
		{
			messageBrokerService.sendMultipleMessages("priorityskills", payloads);
		}
	}
	
	private void createCoursePath(Map<String, List<List<String>>> coursePath, String key, List<String> topics)
	{
		coursePath.put(key, new ArrayList<>());
		for(String topic : topics)
		{
			coursePath.get(key).add(List.of(topic, "www.google.com/"+key));
		}
	}
}