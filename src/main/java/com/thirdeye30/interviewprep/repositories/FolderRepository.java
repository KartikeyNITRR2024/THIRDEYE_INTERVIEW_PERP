package com.thirdeye30.interviewprep.repositories;

import com.thirdeye30.interviewprep.entities.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FolderRepository extends JpaRepository<Folder, UUID> {
    // You can add custom queries here later, like:
    // List<Folder> findByParentUuid(UUID parentUuid);
}