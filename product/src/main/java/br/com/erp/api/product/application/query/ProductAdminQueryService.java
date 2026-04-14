package br.com.erp.api.product.application.query;

import br.com.erp.api.product.application.gateway.StorageGateway;
import br.com.erp.api.product.application.query.filter.ProductFilter;
import br.com.erp.api.product.presentation.dto.response.ProductDetailsDTO;
import br.com.erp.api.product.presentation.dto.response.ProductSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductAdminQueryService {

    private final ProductQueryRepository queryRepository;
    private final StorageGateway storageGateway;

    public ProductAdminQueryService(ProductQueryRepository queryRepository,
                                    StorageGateway storageGateway) {
        this.queryRepository = queryRepository;
        this.storageGateway = storageGateway;
    }

    public Page<ProductSummaryDTO> getAll(ProductFilter filter, Pageable pageable) {

        return queryRepository.findAll(filter, pageable)
                .map(dto -> new ProductSummaryDTO(
                        dto.id(),
                        dto.name(),
                        resolveUrl(dto.mainImageUrl()),
                        dto.slug(),
                        dto.categoryName(),
                        dto.isLaunch(),
                        dto.status()
                ));
    }

    public ProductDetailsDTO getProductById(Long id) {
        ProductDetailsDTO dto = queryRepository.findById(id);
        return new ProductDetailsDTO(
                dto.id(),
                dto.name(),
                dto.description(),
                dto.slug(),
                resolveUrl(dto.mainImageUrl()),
                dto.categoryId(),
                dto.categoryName(),
                dto.isLaunch(),
                dto.status(),
                dto.skus()
        );
    }

    private String resolveUrl(String imageKey) {
        return imageKey != null ? storageGateway.getPublicUrl(imageKey) : null;
    }
}
