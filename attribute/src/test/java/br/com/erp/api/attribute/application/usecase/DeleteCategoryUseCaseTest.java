package br.com.erp.api.attribute.application.usecase;

import br.com.erp.api.attribute.domain.entity.Category;
import br.com.erp.api.attribute.domain.exception.category.CategoryHasAssociatedProductsException;
import br.com.erp.api.attribute.domain.repository.CategoryRepository;
import br.com.erp.api.attribute.domain.valueobject.CategoryName;
import br.com.erp.api.shared.application.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class DeleteCategoryUseCaseTest {

    private CategoryRepository repository;
    private DeleteCategoryUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(CategoryRepository.class);
        useCase = new DeleteCategoryUseCase(repository);
    }

    @Test
    void shouldDeleteCategory_whenNoProductsAssociated() {
        Category category = new Category(1L, new CategoryName("Treino"), true);
        when(repository.findById(1L)).thenReturn(Optional.of(category));
        when(repository.hasProductsAssociated(1L)).thenReturn(false);

        useCase.execute(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void shouldThrow_whenCategoryHasAssociatedProducts() {
        Category category = new Category(1L, new CategoryName("Treino"), true);
        when(repository.findById(1L)).thenReturn(Optional.of(category));
        when(repository.hasProductsAssociated(1L)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(1L))
                .isInstanceOf(CategoryHasAssociatedProductsException.class)
                .hasMessageContaining("Treino");

        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    void shouldThrow_whenCategoryNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}

