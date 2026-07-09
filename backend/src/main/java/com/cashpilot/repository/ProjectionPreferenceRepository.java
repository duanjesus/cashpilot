package com.cashpilot.repository;

import com.cashpilot.entity.ProjectionPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectionPreferenceRepository extends JpaRepository<ProjectionPreference, Long> {

    /** Looked up by the actual owner's own id (never the scope) — this upserts that user's own preference row. */
    Optional<ProjectionPreference> findByUserId(Long userId);

    /** Most recently updated preference across the caller's family-group scope (or just themselves if solo). */
    Optional<ProjectionPreference> findFirstByUserIdInOrderByUpdatedAtDesc(List<Long> userIds);

}
