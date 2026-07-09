package com.cashpilot.repository;

import com.cashpilot.entity.FamilyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FamilyGroupRepository extends JpaRepository<FamilyGroup, Long> {
}
