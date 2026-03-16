package br.com.erp.api.attribute.application.usecase;

import br.com.erp.api.attribute.application.command.CreateCategoryCommand;
import br.com.erp.api.attribute.application.output.CategoryOutput;
import br.com.erp.api.attribute.domain.entity.Category;
import br.com.erp.api.attribute.domain.exception.category.CategoryAlreadyExistsException;
import br.com.erp.api.attribute.domain.repository.CategoryRepository;
import br.com.erp.api.attribute.domain.valueobject.CategoryName;
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

        Optional<Category> existing = categoryRepository.findByNormalizedName(name.normalizedName());

        if (existing.isPresent()) {
            Category category = existing.get();

            if (category.isActive()) {
                throw new CategoryAlreadyExistsException();
            }

            category.activate();
            Category saved = categoryRepository.save(category);

            return new CategoryOutput(
                    saved.getId(),
                    saved.getDisplayName(),
                    saved.isActive()
            );
        }

        Category newCategory = new Category(name);
        Category saved = categoryRepository.save(newCategory);

        return new CategoryOutput(
                saved.getId(),
                saved.getDisplayName(),
                saved.isActive()
        );
    }
}

