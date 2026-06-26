package com.thirdeye30.interviewprep.repositories;

import com.thirdeye30.interviewprep.entities.Explorer;
import com.thirdeye30.interviewprep.enums.Type;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExplorerRepository extends JpaRepository<Explorer, UUID> {
    List<Explorer> findByParentUuid(UUID parentUuid);
    Page<Explorer> findByParentUuid(UUID parentUuid, Pageable pageable);
    Page<Explorer> findByParentUuidAndType(UUID parentUuid, Type type, Pageable pageable);
    Page<Explorer> findByParentUuidIsNull(Pageable pageable);
    Page<Explorer> findByParentUuidIsNullAndType(Type type, Pageable pageable);
    Page<Explorer> findByNameStartingWithIgnoreCase(String name, Pageable pageable);
    boolean existsByParentUuidIsNull();
}