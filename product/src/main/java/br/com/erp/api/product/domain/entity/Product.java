package br.com.erp.api.product.domain.entity;

import br.com.erp.api.product.domain.enumerated.ProductStatus;
import br.com.erp.api.product.domain.exception.ProductAlreadyDeactivatedException;
import br.com.erp.api.product.domain.valueobject.Slug;

public class Product {
    private Long id;
    private String name;
    private String description;
    private Slug slug;
    private Long categoryId;
    private ProductStatus status;

    public Product(String name, String description, Long categoryId) {
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.slug = Slug.fromName(name);
        this.status = ProductStatus.DRAFT;
    }

    public static Product restore(
            long id,
            String name,
            String description,
            Slug slug,
            long categoryId,
            ProductStatus status
    ) {
        Product product = new Product(name, description, categoryId);
        product.id = id;
        product.slug = slug;
        product.status = status;
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

}

