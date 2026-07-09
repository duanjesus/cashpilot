package com.cashpilot.repository;

import com.cashpilot.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    List<BankAccount> findAllByUserIdIn(List<Long> userIds);

    Optional<BankAccount> findByIdAndUserIdIn(Long id, List<Long> userIds);

}
