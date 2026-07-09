package com.cashpilot.repository;

import com.cashpilot.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c WHERE c.user.id IN :userIds OR c.user IS NULL ORDER BY c.nome")
    List<Category> findAllVisibleToUser(@Param("userIds") List<Long> userIds);

    Optional<Category> findByIdAndUserIdIn(Long id, List<Long> userIds);

    boolean existsByUserIdAndNomeIgnoreCase(Long userId, String nome);

}
