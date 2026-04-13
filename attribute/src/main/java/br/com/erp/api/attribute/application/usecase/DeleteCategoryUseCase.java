package br.com.erp.api.attribute.application.usecase;

import br.com.erp.api.attribute.domain.entity.Category;
import br.com.erp.api.attribute.domain.exception.category.CategoryHasAssociatedProductsException;
import br.com.erp.api.attribute.domain.exception.category.CategoryHasSubcategoriesException;
import br.com.erp.api.attribute.domain.repository.CategoryRepository;
import br.com.erp.api.shared.application.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteCategoryUseCase {

    private final CategoryRepository repository;

    public DeleteCategoryUseCase(CategoryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void execute(Long id) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria", id));

        // Categoria pai: não pode excluir se tiver subcategorias
        if (category.isParent() && repository.hasSubcategories(id)) {
            throw new CategoryHasSubcategoriesException(category.getDisplayName());
        }

        // Subcategoria: não pode excluir se tiver produtos associados
        if (repository.hasProductsAssociated(id)) {
            throw new CategoryHasAssociatedProductsException(category.getDisplayName());
        }

        repository.deleteById(id);
    }
}
