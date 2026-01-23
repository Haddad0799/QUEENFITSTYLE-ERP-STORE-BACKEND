package br.com.erp.api.catalog.application.usecase;

import br.com.erp.api.catalog.domain.entity.Category;
import br.com.erp.api.catalog.domain.exception.category.CategoryAlreadyExistsException;
import br.com.erp.api.catalog.domain.repository.CategoryRepository;
import br.com.erp.api.catalog.domain.valueobject.CategoryName;
import br.com.erp.api.shared.application.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RenameCategoryUseCase {

    private final CategoryRepository repository;

    public RenameCategoryUseCase(CategoryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void execute(Long id, String name) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria", id));

        CategoryName newName = validateDuplicateName(name);

        category.rename(newName);


        repository.update(category);
    }

    private CategoryName validateDuplicateName(String name) {
        CategoryName newName = new CategoryName(name);

        if(repository.existsByName(newName.normalizedName())){
            throw new CategoryAlreadyExistsException();
        }
        return newName;
    }
}
