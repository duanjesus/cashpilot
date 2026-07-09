package com.cashpilot.repository;

import com.cashpilot.entity.ProjectionPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectionPreferenceRepository extends JpaRepository<ProjectionPreference, Long> {

    Optional<ProjectionPreference> findByUserId(Long userId);

}
