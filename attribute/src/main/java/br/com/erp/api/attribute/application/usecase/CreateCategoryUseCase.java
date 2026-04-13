package br.com.erp.api.attribute.application.usecase;

import br.com.erp.api.attribute.application.command.CreateCategoryCommand;
import br.com.erp.api.attribute.application.output.CategoryOutput;
import br.com.erp.api.attribute.domain.entity.Category;
import br.com.erp.api.attribute.domain.exception.category.CategoryAlreadyExistsException;
import br.com.erp.api.attribute.domain.exception.category.SubcategoryDepthExceededException;
import br.com.erp.api.attribute.domain.repository.CategoryRepository;
import br.com.erp.api.attribute.domain.valueobject.CategoryName;
import br.com.erp.api.shared.application.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CreateCategoryUseCase {
    private final CategoryRepository categoryRepository;

    public CreateCategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryOutput execute(CreateCategoryCommand command) {
        CategoryName name = new CategoryName(command.name());

        // Valida parentId se informado
        if (command.parentId() != null) {
            Category parent = categoryRepository.findById(command.parentId())
                    .orElseThrow(() -> new EntityNotFoundException("Categoria pai", command.parentId()));

            // Hierarquia máxima de 2 níveis: subcategoria não pode ser pai
            if (parent.isSubcategory()) {
                throw new SubcategoryDepthExceededException();
            }
        }

        Optional<Category> existing = categoryRepository.findByNormalizedName(name.normalizedName());

        if (existing.isPresent()) {
            Category category = existing.get();

            if (category.isActive()) {
                throw new CategoryAlreadyExistsException();
            }

            category.activate();
            Category saved = categoryRepository.save(category);

            return toOutput(saved);
        }

        Category newCategory = command.parentId() != null
                ? new Category(name, command.parentId())
                : new Category(name);

        Category saved = categoryRepository.save(newCategory);

        return toOutput(saved);
    }

    private CategoryOutput toOutput(Category category) {
        return new CategoryOutput(
                category.getId(),
                category.getDisplayName(),
                category.getNormalizedName(),
                category.isActive(),
                category.getParentId()
        );
    }
}
