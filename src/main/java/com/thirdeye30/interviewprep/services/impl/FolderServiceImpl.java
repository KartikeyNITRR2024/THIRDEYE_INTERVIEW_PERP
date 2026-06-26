package com.thirdeye30.interviewprep.services.impl;

import com.thirdeye30.interviewprep.dtos.ExplorerDto;
import com.thirdeye30.interviewprep.dtos.FolderDto;
import com.thirdeye30.interviewprep.entities.Explorer;
import com.thirdeye30.interviewprep.entities.File;
import com.thirdeye30.interviewprep.entities.Folder;
import com.thirdeye30.interviewprep.enums.ActionType;
import com.thirdeye30.interviewprep.enums.Type;
import com.thirdeye30.interviewprep.repositories.ExplorerRepository;
import com.thirdeye30.interviewprep.repositories.FolderRepository;
import com.thirdeye30.interviewprep.services.ExplorerService;
import com.thirdeye30.interviewprep.services.FolderService;
import com.thirdeye30.interviewprep.utils.Mapper;
import com.thirdeye30.interviewprep.utils.PageCacheWrapper;
import com.thirdeye30.interviewprep.utils.Utils;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;
    private final ExplorerRepository explorerRepository;
    private final Utils utils;
    private final ExplorerService explorerService;
    
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${thirdeye.redis.response.enabled:false}")
    private boolean isRedisResponseCacheEnabled;

    private final String redisFolderPrefix = "folder-service:folder:";
    private final String redisFolderPagePrefix = "folder-service:pages:";
    private final Duration cacheDuration = Duration.ofMinutes(30);
    private final Duration pageCacheDuration = Duration.ofMinutes(5);

    @Override
    @Transactional
    public FolderDto createFolder(FolderDto folderDto) {
        Folder folder = Mapper.mapToEntity(folderDto);
        Folder savedFolder = folderRepository.save(folder);
        explorerService.recordAction(folder.getParentUuid(), ActionType.ADD_FOLDER);
        
        FolderDto dto = Mapper.mapToDto(savedFolder);

        if (isRedisResponseCacheEnabled) {
            try {
                redisTemplate.opsForValue().set(redisFolderPrefix + savedFolder.getUuid(), dto, cacheDuration);
            } catch (Exception e) {
                log.error("Redis write failed during folder creation for: {}", savedFolder.getUuid(), e);
            }
        }
        
        evictParentPageCache(dto.getParentUuid());

        return dto;
    }

    @Override
    public FolderDto getFolderById(UUID id) {
        String cacheKey = redisFolderPrefix + id;

        if (isRedisResponseCacheEnabled) {
            try {
                FolderDto cachedDto = (FolderDto) redisTemplate.opsForValue().get(cacheKey);
                if (cachedDto != null) {
                    return cachedDto;
                }
            } catch (Exception e) {
                log.error("Redis read failed during folder cache check for: {}", id, e);
            }
        }

        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Folder not found with ID: " + id));
                
        FolderDto dto = Mapper.mapToDto(folder);

        if (isRedisResponseCacheEnabled) {
            try {
                redisTemplate.opsForValue().set(cacheKey, dto, cacheDuration);
            } catch (Exception e) {
                log.error("Failed to write folder cache to Redis for key: {}", id, e);
            }
        }

        return dto;
    }

    @Override
    public List<FolderDto> getAllFolders() {
        return folderRepository.findAll()
                .stream()
                .map(Mapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FolderDto updateFolder(UUID id, FolderDto folderDto) {
        Folder existingFolder = folderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Folder not found with ID: " + id));
        
        existingFolder.setName(folderDto.getName());
        existingFolder.setParentUuid(folderDto.getParentUuid());

        if (folderDto.getNoOfFiles() != null) existingFolder.setNoOfFiles(folderDto.getNoOfFiles());
        if (folderDto.getNoOfFolders() != null) existingFolder.setNoOfFolders(folderDto.getNoOfFolders());

        Folder updatedFolder = folderRepository.save(existingFolder);
        FolderDto dto = Mapper.mapToDto(updatedFolder);

        if (isRedisResponseCacheEnabled) {
            try {
                redisTemplate.opsForValue().set(redisFolderPrefix + id, dto, cacheDuration);
            } catch (Exception e) {
                log.error("Redis update failed for folder: {}", id, e);
            }
        }
        evictParentPageCache(id);
        evictParentPageCache(dto.getParentUuid());

        return dto;
    }

    @Override
    @Transactional
    public void deleteFolder(UUID id) {
        if (!folderRepository.existsById(id)) {
            throw new EntityNotFoundException("Folder not found with ID: " + id);
        }
        folderRepository.deleteById(id);

        if (isRedisResponseCacheEnabled) {
            try {
                redisTemplate.delete(redisFolderPrefix + id);
            } catch (Exception e) {
                log.error("Redis eviction failed for folder: {}", id, e);
            }
        }
        
        evictParentPageCache(id);
    }
    
    @Override
    public List<ExplorerDto> getFolderContents(UUID parentUuid) {
        List<Explorer> contents = explorerRepository.findByParentUuid(parentUuid);
        
        return contents.stream()
            .<ExplorerDto>map(explorer -> {
                if (explorer instanceof Folder) {
                    return Mapper.mapToDto((Folder) explorer); 
                } else if (explorer instanceof File) {
                    return Mapper.mapToDto((File) explorer, "");
                }
                throw new IllegalStateException("Unknown explorer type");
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public Page<ExplorerDto> getFolderContents(UUID parentUuid, Type typeFilter, Pageable pageable) {
        String typeStr = typeFilter != null ? typeFilter.name() : "ALL";
        String sortStr = pageable.getSort().isSorted() ? pageable.getSort().toString().replace(":", "-").replace(" ", "") : "UNSORTED";
        String cacheKey = String.format("%sparent:%s:type:%s:page:%d:size:%d:sort:%s", 
                redisFolderPagePrefix, parentUuid, typeStr, pageable.getPageNumber(), pageable.getPageSize(), sortStr);

        if (isRedisResponseCacheEnabled) {
            try {
                PageCacheWrapper cached = (PageCacheWrapper) redisTemplate.opsForValue().get(cacheKey);
                if (cached != null) {
                    return new PageImpl<>(cached.getContent(), pageable, cached.getTotalElements());
                }
            } catch (Exception e) {
                log.error("Redis read failed during paginated cache check for key: {}", cacheKey, e);
            }
        }

        Page<Explorer> explorerPage;
        if (typeFilter == null) {
            explorerPage = explorerRepository.findByParentUuid(parentUuid, pageable);
        } else {
            explorerPage = explorerRepository.findByParentUuidAndType(parentUuid, typeFilter, pageable);
        }
        
        Page<ExplorerDto> resultPage = explorerPage.map(explorer -> {
            if (explorer instanceof Folder) {
                return Mapper.mapToDto((Folder) explorer); 
            } else if (explorer instanceof File) {
                File file = (File) explorer;
                return Mapper.mapToDto(file, file.getInternalDocumentId() != null ? utils.documentUrlGenerator(file.getInternalDocumentId()) : file.getExternalUrl());
            }
            throw new IllegalStateException("Unknown explorer type");
        });

        if (isRedisResponseCacheEnabled) {
            try {
                PageCacheWrapper wrapper = new PageCacheWrapper(resultPage.getContent(), resultPage.getTotalElements());
                redisTemplate.opsForValue().set(cacheKey, wrapper, pageCacheDuration);
            } catch (Exception e) {
                log.error("Failed to write paginated cache to Redis for key: {}", cacheKey, e);
            }
        }

        return resultPage;
    }
    
    @Override
    public Page<ExplorerDto> getRootContents(Type typeFilter, Pageable pageable) {
        String typeStr = typeFilter != null ? typeFilter.name() : "ALL";
        String sortStr = pageable.getSort().isSorted() ? pageable.getSort().toString().replace(":", "-").replace(" ", "") : "UNSORTED";
        String cacheKey = String.format("%sroot:type:%s:page:%d:size:%d:sort:%s", 
                redisFolderPagePrefix, typeStr, pageable.getPageNumber(), pageable.getPageSize(), sortStr);

        if (isRedisResponseCacheEnabled) {
            try {
                PageCacheWrapper cached = (PageCacheWrapper) redisTemplate.opsForValue().get(cacheKey);
                if (cached != null) {
                    return new PageImpl<>(cached.getContent(), pageable, cached.getTotalElements());
                }
            } catch (Exception e) {
                log.error("Redis read failed during paginated root cache check for key: {}", cacheKey, e);
            }
        }

        Page<Explorer> explorerPage;
        if (typeFilter == null) {
            explorerPage = explorerRepository.findByParentUuidIsNull(pageable);
        } else {
            explorerPage = explorerRepository.findByParentUuidIsNullAndType(typeFilter, pageable);
        }
        
        Page<ExplorerDto> resultPage = explorerPage.map(explorer -> {
            if (explorer instanceof Folder) {
                return Mapper.mapToDto((Folder) explorer); 
            } else if (explorer instanceof File) {
                File file = (File) explorer;
                return Mapper.mapToDto(file, file.getInternalDocumentId() != null ? utils.documentUrlGenerator(file.getInternalDocumentId()) : file.getExternalUrl());
            }
            throw new IllegalStateException("Unknown explorer type");
        });

        if (isRedisResponseCacheEnabled) {
            try {
                PageCacheWrapper wrapper = new PageCacheWrapper(resultPage.getContent(), resultPage.getTotalElements());
                redisTemplate.opsForValue().set(cacheKey, wrapper, pageCacheDuration);
            } catch (Exception e) {
                log.error("Failed to write paginated root cache to Redis for key: {}", cacheKey, e);
            }
        }

        return resultPage;
    }
    
    @Override
    public void evictParentPageCache(UUID parentUuid) {
        if (!isRedisResponseCacheEnabled) {
            return;
        }

        try {
            String cachePattern;
            if (parentUuid != null) {
                cachePattern = redisFolderPagePrefix + "parent:" + parentUuid + ":*";
            } else {
                cachePattern = redisFolderPagePrefix + "root:*";
            }
            java.util.Set<String> keysToDelete = redisTemplate.keys(cachePattern);
            if (keysToDelete != null && !keysToDelete.isEmpty()) {
                redisTemplate.delete(keysToDelete);
            }
        } catch (Exception e) {
            log.error("Failed to evict paginated cache for parent folder: {}", parentUuid, e);
        }
    }

}