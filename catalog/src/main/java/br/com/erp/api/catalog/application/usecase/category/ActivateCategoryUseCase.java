package br.com.erp.api.catalog.application.usecase.category;

import br.com.erp.api.catalog.domain.entity.Category;
import br.com.erp.api.catalog.domain.repository.CategoryRepository;
import br.com.erp.api.shared.domain.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivateCategoryUseCase {

    private final CategoryRepository repository;

    public ActivateCategoryUseCase( CategoryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void execute(Long id) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria", id));

        category.activate();

        repository.update(category);
    }
}
