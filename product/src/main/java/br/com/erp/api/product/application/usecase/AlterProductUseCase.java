package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.command.AlterProductCommand;
import br.com.erp.api.product.application.exception.InvalidCategoryException;
import br.com.erp.api.product.application.exception.ProductNotFoundException;
import br.com.erp.api.product.application.output.ProductDetailsOutput;
import br.com.erp.api.product.domain.entity.Product;
import br.com.erp.api.product.domain.port.CategoryLookupPort;
import br.com.erp.api.product.domain.port.ProductRepository;
import br.com.erp.api.product.domain.valueobject.CategoryId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlterProductUseCase {

   private final CategoryLookupPort categoryLookupPort;
   private final ProductRepository productRepository;

    public AlterProductUseCase(CategoryLookupPort categoryLookupPort, ProductRepository productRepository) {
        this.categoryLookupPort = categoryLookupPort;
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductDetailsOutput execute(AlterProductCommand command) {

        Product product = productRepository
                .findById(command.productId())
                .orElseThrow(()-> new ProductNotFoundException("Produto", command.productId()));

        if(command.name() != null) {
            product.rename(command.name());
        }

        if(command.description() != null) {
            product.changeDescription(command.description());
        }

        if(command.categoryId() != null) {

            if(!categoryLookupPort.existsActiveById(command.categoryId())) {
                throw new InvalidCategoryException("Categoiria", command.categoryId());
            }
            product.recategorize(new CategoryId(command.categoryId()));
        }

        productRepository.update(product);

        return new ProductDetailsOutput(product.getId(),
                product.getName(),
                product.getSlugValue(),
                product.getCategoryId().value(),
                product.isActive());
    }

}
