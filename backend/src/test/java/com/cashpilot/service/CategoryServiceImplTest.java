package com.cashpilot.service;

import com.cashpilot.dto.request.CategoryRequestDTO;
import com.cashpilot.dto.response.CategoryResponseDTO;
import com.cashpilot.entity.Category;
import com.cashpilot.entity.User;
import com.cashpilot.entity.enums.CategoryType;
import com.cashpilot.exception.BusinessException;
import com.cashpilot.exception.DuplicateResourceException;
import com.cashpilot.exception.ResourceNotFoundException;
import com.cashpilot.mapper.CategoryMapper;
import com.cashpilot.repository.CategoryRepository;
import com.cashpilot.repository.ExpenseRepository;
import com.cashpilot.repository.IncomeRepository;
import com.cashpilot.security.CurrentUserProvider;
import com.cashpilot.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService")
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private User user;
    private CategoryRequestDTO requestDTO;
    private Category category;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Ana").email("ana@cashpilot.com").password("hash").build();
        requestDTO = new CategoryRequestDTO("Streaming", CategoryType.DESPESA, "#ef4444");
        category = Category.builder()
                .id(10L)
                .user(user)
                .nome(requestDTO.nome())
                .tipo(requestDTO.tipo())
                .cor(requestDTO.cor())
                .isSystem(false)
                .isInvestment(false)
                .build();
    }

    @Test
    @DisplayName("Deve criar categoria com sucesso quando nome não existe para o usuário")
    void deveCriarCategoriaComSucesso() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(categoryRepository.existsByUserIdAndNomeIgnoreCase(1L, requestDTO.nome())).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toResponseDto(category)).thenReturn(
                new CategoryResponseDTO(10L, requestDTO.nome(), requestDTO.tipo(), false, false, requestDTO.cor()));

        CategoryResponseDTO response = categoryService.create(requestDTO);

        assertThat(response).isNotNull();
        assertThat(response.nome()).isEqualTo("Streaming");
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Deve lançar DuplicateResourceException ao criar categoria com nome já existente")
    void deveLancarExcecaoAoCriarCategoriaDuplicada() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(categoryRepository.existsByUserIdAndNomeIgnoreCase(1L, requestDTO.nome())).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(requestDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining(requestDTO.nome());

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao tentar excluir categoria do sistema")
    void deveLancarExcecaoAoExcluirCategoriaDoSistema() {
        Category systemCategory = Category.builder().id(2L).user(null).nome("Salário")
                .tipo(CategoryType.RECEITA).isSystem(true).isInvestment(false).build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(systemCategory));

        assertThatThrownBy(() -> categoryService.delete(2L))
                .isInstanceOf(BusinessException.class);

        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao excluir categoria em uso por despesas")
    void deveLancarExcecaoAoExcluirCategoriaEmUso() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(incomeRepository.existsByCategoriaId(10L)).thenReturn(false);
        when(expenseRepository.existsByCategoriaId(10L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.delete(10L))
                .isInstanceOf(BusinessException.class);

        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar categoria inexistente")
    void deveLancarExcecaoAoBuscarCategoriaInexistente() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

}
