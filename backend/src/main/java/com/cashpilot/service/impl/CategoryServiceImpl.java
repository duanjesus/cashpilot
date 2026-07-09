package com.cashpilot.service.impl;

import com.cashpilot.dto.request.CategoryRequestDTO;
import com.cashpilot.dto.response.CategoryResponseDTO;
import com.cashpilot.entity.Category;
import com.cashpilot.entity.User;
import com.cashpilot.exception.BusinessException;
import com.cashpilot.exception.DuplicateResourceException;
import com.cashpilot.exception.ResourceNotFoundException;
import com.cashpilot.mapper.CategoryMapper;
import com.cashpilot.repository.CategoryRepository;
import com.cashpilot.repository.ExpenseRepository;
import com.cashpilot.repository.IncomeRepository;
import com.cashpilot.security.CurrentUserProvider;
import com.cashpilot.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final CategoryMapper categoryMapper;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public CategoryResponseDTO create(CategoryRequestDTO dto) {
        User user = currentUserProvider.getCurrentUser();

        if (categoryRepository.existsByUserIdAndNomeIgnoreCase(user.getId(), dto.nome())) {
            throw new DuplicateResourceException("Já existe uma categoria com o nome: " + dto.nome());
        }

        Category category = Category.builder()
                .user(user)
                .nome(dto.nome())
                .tipo(dto.tipo())
                .cor(dto.cor())
                .isSystem(false)
                .isInvestment(false)
                .build();

        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponseDto(saved);
    }

    @Override
    public CategoryResponseDTO update(Long id, CategoryRequestDTO dto) {
        Category category = findOwnedEntityById(id);

        category.setNome(dto.nome());
        category.setTipo(dto.tipo());
        category.setCor(dto.cor());

        Category updated = categoryRepository.save(category);
        return categoryMapper.toResponseDto(updated);
    }

    @Override
    public void delete(Long id) {
        Category category = findOwnedEntityById(id);

        boolean inUse = incomeRepository.existsByCategoriaId(id) || expenseRepository.existsByCategoriaId(id);
        if (inUse) {
            throw new BusinessException("Não é possível excluir uma categoria em uso por receitas ou despesas");
        }

        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO findById(Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        Category category = categoryRepository.findById(id)
                .filter(c -> c.getUser() == null || c.getUser().getId().equals(userId))
                .orElseThrow(() -> ResourceNotFoundException.of("Categoria", id));
        return categoryMapper.toResponseDto(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> findAllVisible() {
        Long userId = currentUserProvider.getCurrentUserId();
        return categoryRepository.findAllVisibleToUser(userId).stream()
                .map(categoryMapper::toResponseDto)
                .toList();
    }

    private Category findOwnedEntityById(Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Categoria", id));

        if (Boolean.TRUE.equals(category.getIsSystem())) {
            throw new BusinessException("Categorias padrão do sistema não podem ser editadas ou excluídas");
        }
        if (category.getUser() == null || !category.getUser().getId().equals(userId)) {
            throw ResourceNotFoundException.of("Categoria", id);
        }
        return category;
    }

}
