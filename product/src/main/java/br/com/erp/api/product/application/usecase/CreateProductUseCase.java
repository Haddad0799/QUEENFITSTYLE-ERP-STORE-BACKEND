package br.com.erp.api.product.application.usecase;

import br.com.erp.api.catalog.application.exception.CategoryNotFoundException;
import br.com.erp.api.product.application.command.CreateProductCommand;
import br.com.erp.api.product.application.output.ProductDetailsOutput;
import br.com.erp.api.product.domain.entity.Product;
import br.com.erp.api.product.domain.exception.ProductDuplicateException;
import br.com.erp.api.product.domain.port.CategoryLookupPort;
import br.com.erp.api.product.domain.port.ProductRepository;
import br.com.erp.api.product.domain.valueobject.CategoryId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateProductUseCase {

    private final ProductRepository productRepository;
    private final CategoryLookupPort categoryLookup;

    public CreateProductUseCase(ProductRepository productRepository, CategoryLookupPort categoryLookup) {
        this.productRepository = productRepository;
        this.categoryLookup = categoryLookup;
    }

    @Transactional
    public ProductDetailsOutput execute(CreateProductCommand command) {

        CategoryId categoryId = validateCategory(command.categoryId());

        Product product = new Product(
                command.name(),
                command.description(),
                categoryId
        );

        validateDuplicateProduct(product);

        Product saved = productRepository.save(product);

        return new ProductDetailsOutput(
                saved.getId(),
                saved.getName(),
                saved.getSlugValue(),
                saved.getCategoryId().value(),
                saved.isActive()
        );
    }

    private CategoryId validateCategory(Long id) {
        CategoryId categoryId = new CategoryId(id);

        if (!categoryLookup.existsActiveById(categoryId.value())) {
            throw new CategoryNotFoundException("Categoria", id);
        }

        return categoryId;
    }

    private void validateDuplicateProduct(Product product) {

        if (productRepository.existsByslug(product.getSlugValue())) {
            throw new ProductDuplicateException("Produto duplicado.");
        }
    }

}

