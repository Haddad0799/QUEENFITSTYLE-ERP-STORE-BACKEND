package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.command.CreateProductCommand;
import br.com.erp.api.product.domain.entity.Product;
import br.com.erp.api.product.domain.exception.InvalidCategoryException;
import br.com.erp.api.product.domain.exception.ProductDuplicateException;
import br.com.erp.api.product.domain.port.CategoryLookupPort;
import br.com.erp.api.product.domain.port.ProductRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateProductUseCase {

    private final ProductRepositoryPort productRepository;
    private final CategoryLookupPort categoryLookup;

    public CreateProductUseCase(ProductRepositoryPort productRepository, CategoryLookupPort categoryLookup) {
        this.productRepository = productRepository;
        this.categoryLookup = categoryLookup;
    }

    @Transactional
    public Long execute(CreateProductCommand command) {

        Long categoryId = validateCategory(command.categoryId());

        Product product = new Product(
                command.name(),
                command.description(),
                categoryId
        );

        validateDuplicateProduct(product);

        return productRepository.save(product);
    }

    private Long validateCategory(Long id) {

        if (categoryLookup.existsActiveById(id)) {
            throw new InvalidCategoryException();
        }
        return id;
    }

    private void validateDuplicateProduct(Product product) {

        if (productRepository.existsByslug(product.getSlugValue())) {
            throw new ProductDuplicateException();
        }
    }

}

