package com.cashpilot.repository;

import com.cashpilot.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByIdAndUserIdIn(Long id, List<Long> userIds);

    List<Subscription> findAllByUserIdIn(List<Long> userIds);

    List<Subscription> findAllByUserIdInAndAtivaTrue(List<Long> userIds);

    /** System-wide, no user scoping — used only by the background scheduler. */
    List<Subscription> findAllByAtivaTrue();

}
