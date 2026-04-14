package br.com.erp.api.product.domain.entity;

import br.com.erp.api.product.domain.enumerated.ProductStatus;
import br.com.erp.api.product.domain.exception.ProductAlreadyDeactivatedException;
import br.com.erp.api.product.domain.valueobject.Slug;

import java.util.List;

public class Product {
    private Long id;
    private String name;
    private String description;
    private boolean isLaunch;
    private Long primaryImageId;
    private Slug slug;
    private Long categoryId;
    private ProductStatus status;

    private Product() {}

    public Product(String name, String description, Long categoryId, boolean isLaunch) {
        this.name = name;
        this.description = description;
        this.isLaunch = isLaunch;
        this.categoryId = categoryId;
        this.slug = Slug.fromName(name);
        this.status = ProductStatus.DRAFT;
        this.primaryImageId = null;
    }

    public static Product createWithSlug(String name, String slug, Long categoryId) {
        return createWithSlug(name, slug, categoryId, false);
    }

    public static Product createWithSlug(String name, String slug, Long categoryId, boolean isLaunch) {
        Product product = new Product();
        product.name = name;
        product.slug = Slug.fromValue(slug);
        product.categoryId = categoryId;
        product.isLaunch = isLaunch;
        product.status = ProductStatus.DRAFT;
        return product;
    }

    public static Product restore(long id, String name, String description,
                                  Slug slug, long categoryId, ProductStatus status,
                                  boolean isLaunch, Long primaryImageId) {
        Product product = new Product();
        product.id = id;
        product.name = name;
        product.description = description;
        product.slug = slug;
        product.categoryId = categoryId;
        product.status = status;
        product.isLaunch = isLaunch;
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

    public void changeLaunch(boolean isLaunch) {
        this.isLaunch = isLaunch;
    }

    public void assertDeletable() {
        if (this.status == ProductStatus.PUBLISHED) {
            throw new IllegalStateException("Produto publicado não pode ser excluído. Despublique-o antes.");
        }
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
        if (this.status == ProductStatus.READY_FOR_SALE ||
                this.status == ProductStatus.PUBLISHED) {
            this.status = ProductStatus.DRAFT;
        }
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

    public boolean isPublished() {
        return this.status == ProductStatus.PUBLISHED;
    }

    public boolean isInactive() {
        return this.status == ProductStatus.INACTIVE;
    }

    public boolean isArchived() {
        return this.status == ProductStatus.ARCHIVED;
    }

    public boolean isDraft() {
        return this.status == ProductStatus.DRAFT;
    }

    public boolean isReadyForSale() {
        return this.status == ProductStatus.READY_FOR_SALE;
    }

    public boolean isLaunch() {
        return isLaunch;
    }
}
