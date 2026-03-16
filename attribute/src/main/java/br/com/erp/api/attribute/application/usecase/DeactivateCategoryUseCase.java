package br.com.erp.api.attribute.application.usecase;

import br.com.erp.api.attribute.domain.entity.Category;
import br.com.erp.api.attribute.domain.repository.CategoryRepository;
import br.com.erp.api.shared.application.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeactivateCategoryUseCase {

    private final CategoryRepository repository;

    public DeactivateCategoryUseCase(CategoryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void execute(Long id) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria", id));

        category.deactivate();

        repository.update(category);
    }
}

