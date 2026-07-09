package com.cashpilot.repository;

import com.cashpilot.entity.FamilyGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyGroupMemberRepository extends JpaRepository<FamilyGroupMember, Long> {

    Optional<FamilyGroupMember> findByUserId(@Param("userId") Long userId);

    List<FamilyGroupMember> findAllByFamilyGroupId(@Param("familyGroupId") Long familyGroupId);

    Optional<FamilyGroupMember> findByFamilyGroupIdAndUserId(Long familyGroupId, Long userId);

    boolean existsByUserId(Long userId);

}
