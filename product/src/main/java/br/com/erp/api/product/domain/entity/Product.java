package br.com.erp.api.product.domain.entity;

import br.com.erp.api.product.domain.enumerated.ProductStatus;
import br.com.erp.api.product.domain.exception.ProductAlreadyDeactivatedException;
import br.com.erp.api.product.domain.valueobject.Slug;

import java.util.List;

public class Product {
    private Long id;
    private String name;
    private String description;
    private Long primaryImageId;
    private Slug slug;
    private Long categoryId;
    private ProductStatus status;

    public Product(String name, String description, Long categoryId) {
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.slug = Slug.fromName(name);
        this.status = ProductStatus.DRAFT;
        this.primaryImageId = null;
    }

    public static Product restore(long id, String name, String description,
                                  Slug slug, long categoryId, ProductStatus status, Long primaryImageId) {
        Product product = new Product(name, description, categoryId);
        product.id = id;
        product.slug = slug;
        product.status = status;
        product.primaryImageId = primaryImageId;
        return product;
    }

    public ProductStatus getStatus() {
        return this.status;
    }

    public void rename(String newName) {
        this.name = newName;
        this.slug = Slug.fromName(newName);
    }

    public void changeDescription(String newDescription) {
        this.description = newDescription;
    }

    public void recategorize(Long newCategoryId) {
        this.categoryId = newCategoryId;
    }

    public void publish() {
        if (this.status == ProductStatus.ARCHIVED) {
            throw new IllegalStateException("Produto arquivado não pode ser publicado");
        }
        this.status = ProductStatus.PUBLISHED;
    }

    public void markAsReadyForSale() {
        if (this.status == ProductStatus.DRAFT) {
            this.status = ProductStatus.READY_FOR_SALE;
        }
    }

    public void markAsDraft() {
        if (this.status == ProductStatus.READY_FOR_SALE) {
            this.status = ProductStatus.DRAFT;
        }
    }

    public boolean isDraft() {
        return this.status == ProductStatus.DRAFT;
    }

    public boolean isReadyForSale() {
        return this.status == ProductStatus.READY_FOR_SALE;
    }

    public void deactivate() {
        if (this.status == ProductStatus.INACTIVE) {
            throw new ProductAlreadyDeactivatedException();
        }
        this.status = ProductStatus.INACTIVE;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSlugValue() {
        return slug.value();
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void definePrimaryImage(Long imageId) {
        this.primaryImageId = imageId;
    }

    public void clearPrimaryImageIfMatch(Long imageId) {
        if (imageId != null && imageId.equals(this.primaryImageId)) {
            this.primaryImageId = null;
        }
    }

    public boolean isPrimaryImageAmong(List<Long> imageIds) {
        return primaryImageId != null && imageIds.contains(primaryImageId);
    }

    public Long getPrimaryImageId() { return primaryImageId; }

}
