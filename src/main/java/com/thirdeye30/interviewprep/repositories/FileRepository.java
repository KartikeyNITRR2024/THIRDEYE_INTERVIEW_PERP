package com.thirdeye30.interviewprep.repositories;

import com.thirdeye30.interviewprep.entities.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<File, UUID> {
    // List<File> findByParentUuid(UUID parentUuid);
}
