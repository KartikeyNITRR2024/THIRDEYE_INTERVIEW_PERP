package com.thirdeye30.interviewprep.utils;

import com.thirdeye30.interviewprep.entities.Folder;
import com.thirdeye30.interviewprep.enums.Type;
import com.thirdeye30.interviewprep.repositories.ExplorerRepository;
import com.thirdeye30.interviewprep.repositories.FolderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Initiator implements CommandLineRunner {

    private final ExplorerRepository explorerRepository;
    private final FolderRepository folderRepository;
    
    @Value("${thirdeye.root.name:System_Root}")
    private String rootFolderName;

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking for Root file system directory...");
        if (!explorerRepository.existsByParentUuidIsNull()) {
            log.info("Root directory not found. Creating ROOT folder with name: {}", rootFolderName);
            Folder rootFolder = new Folder();
            rootFolder.setName(rootFolderName);
            rootFolder.setParentUuid(null);
            rootFolder.setType(Type.FOLDER);
            rootFolder.setNoOfFiles(0);
            rootFolder.setNoOfFolders(0);
            folderRepository.save(rootFolder);
            log.info("ROOT folder initialized successfully.");
        } else {
            log.info("ROOT directory already exists. Skipping initialization.");
        }
    }
}
