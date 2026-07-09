package com.cashpilot.repository;

import com.cashpilot.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByIdAndUserId(Long id, Long userId);

    List<Subscription> findAllByUserId(Long userId);

    List<Subscription> findAllByUserIdAndAtivaTrue(Long userId);

    /** System-wide, no user scoping — used only by the background scheduler. */
    List<Subscription> findAllByAtivaTrue();

}
