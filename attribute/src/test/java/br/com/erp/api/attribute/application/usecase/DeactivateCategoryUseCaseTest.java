package br.com.erp.api.attribute.application.usecase;

import br.com.erp.api.attribute.domain.entity.Category;
import br.com.erp.api.attribute.domain.exception.category.CategoryAlreadyInactiveException;
import br.com.erp.api.attribute.domain.exception.category.CategoryHasActiveSubcategoriesException;
import br.com.erp.api.attribute.domain.exception.category.CategoryHasPublishedProductsException;
import br.com.erp.api.attribute.domain.repository.CategoryRepository;
import br.com.erp.api.attribute.domain.valueobject.CategoryName;
import br.com.erp.api.shared.application.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class DeactivateCategoryUseCaseTest {

    private CategoryRepository repository;
    private DeactivateCategoryUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(CategoryRepository.class);
        useCase = new DeactivateCategoryUseCase(repository);
    }

    @Test
    void shouldDeactivateSubcategory_whenNoPublishedProducts() {
        // Subcategoria (parentId = 10L)
        Category subcategory = new Category(1L, new CategoryName("Legging"), true, 10L);
        when(repository.findById(1L)).thenReturn(Optional.of(subcategory));
        when(repository.hasPublishedProducts(1L)).thenReturn(false);

        useCase.execute(1L);

        verify(repository).update(subcategory);
    }

    @Test
    void shouldThrow_whenSubcategoryHasPublishedProducts() {
        Category subcategory = new Category(1L, new CategoryName("Legging"), true, 10L);
        when(repository.findById(1L)).thenReturn(Optional.of(subcategory));
        when(repository.hasPublishedProducts(1L)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(1L))
                .isInstanceOf(CategoryHasPublishedProductsException.class)
                .hasMessageContaining("Legging");

        verify(repository, never()).update(any());
    }

    @Test
    void shouldDeactivateParentCategory_whenNoActiveSubcategories() {
        // Categoria pai (parentId = null)
        Category parent = new Category(1L, new CategoryName("Roupas"), true);
        when(repository.findById(1L)).thenReturn(Optional.of(parent));
        when(repository.hasActiveSubcategories(1L)).thenReturn(false);

        useCase.execute(1L);

        verify(repository).update(parent);
    }

    @Test
    void shouldThrow_whenParentCategoryHasActiveSubcategories() {
        Category parent = new Category(1L, new CategoryName("Roupas"), true);
        when(repository.findById(1L)).thenReturn(Optional.of(parent));
        when(repository.hasActiveSubcategories(1L)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(1L))
                .isInstanceOf(CategoryHasActiveSubcategoriesException.class)
                .hasMessageContaining("Roupas");

        verify(repository, never()).update(any());
    }

    @Test
    void shouldThrow_whenCategoryAlreadyInactive() {
        Category category = new Category(1L, new CategoryName("Treino"), false);
        when(repository.findById(1L)).thenReturn(Optional.of(category));
        when(repository.hasActiveSubcategories(1L)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(1L))
                .isInstanceOf(CategoryAlreadyInactiveException.class);
    }

    @Test
    void shouldThrow_whenCategoryNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
