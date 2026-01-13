package br.com.erp.api.catalog.application.usecase.category;

import br.com.erp.api.catalog.application.command.category.CreateCategoryCommand;
import br.com.erp.api.catalog.application.output.category.CategoryCreatedOutput;
import br.com.erp.api.catalog.domain.entity.Category;
import br.com.erp.api.catalog.domain.exception.category.CategoryAlreadyExistsException;
import br.com.erp.api.catalog.domain.repository.CategoryRepository;
import br.com.erp.api.catalog.domain.valueobject.Name;
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
    public CategoryCreatedOutput execute(CreateCategoryCommand command) {
        Name name = new Name(command.name());

        Optional<Category> existing = categoryRepository.findByNormalizedName(name.normalizedName());

        if (existing.isPresent()) {
            Category category = existing.get();

            if (category.isActive()) {
                throw new CategoryAlreadyExistsException();
            }

            category.activate();
            Category saved = categoryRepository.save(category);

            return new CategoryCreatedOutput(
                    saved.getId(),
                    saved.getDisplayName(),
                    saved.isActive()
            );
        }

        Category newCategory = new Category(name);
        Category saved = categoryRepository.save(newCategory);

        return new CategoryCreatedOutput(
                saved.getId(),
                saved.getDisplayName(),
                saved.isActive()
        );
    }
}
