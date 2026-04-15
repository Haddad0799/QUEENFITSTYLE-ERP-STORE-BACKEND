package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.command.AlterProductCommand;
import br.com.erp.api.product.application.exception.ProductNotFoundException;
import br.com.erp.api.product.application.port.ProductCatalogPort;
import br.com.erp.api.product.application.service.ProductCatalogPublisher;
import br.com.erp.api.product.domain.entity.Product;
import br.com.erp.api.product.domain.exception.InvalidCategoryException;
import br.com.erp.api.product.application.provider.CategoryProvider;
import br.com.erp.api.product.domain.port.ProductRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlterProductUseCase {

   private final CategoryProvider categoryProvider;
   private final ProductRepositoryPort productRepository;
   private final ProductCatalogPort productCatalogPublisher;

    public AlterProductUseCase(CategoryProvider categoryProvider, ProductRepositoryPort productRepository, ProductCatalogPublisher productCatalogPublisher) {
        this.categoryProvider = categoryProvider;
        this.productRepository = productRepository;
        this.productCatalogPublisher = productCatalogPublisher;
    }

    @Transactional
    public void execute(AlterProductCommand command) {

        Product product = productRepository
                .findById(command.productId())
                .orElseThrow(()-> new ProductNotFoundException(command.productId()));

        if(command.name() != null) {
            product.rename(command.name());
        }

        if(command.description() != null) {
            product.changeDescription(command.description());
        }

        if(command.categoryId() != null) {

            if(!categoryProvider.existsActiveById(command.categoryId())) {
                throw new InvalidCategoryException();
            }
            product.recategorize(command.categoryId());
        }

        if (command.isLaunch() != null) {
            product.changeLaunch(command.isLaunch());
        }

        productRepository.update(product);

        productCatalogPublisher.publishIfPublished(product.getId());
    }
}
