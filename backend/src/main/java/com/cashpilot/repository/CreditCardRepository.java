package com.cashpilot.repository;

import com.cashpilot.entity.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {

    List<CreditCard> findAllByUserIdIn(List<Long> userIds);

    Optional<CreditCard> findByIdAndUserIdIn(Long id, List<Long> userIds);

}
