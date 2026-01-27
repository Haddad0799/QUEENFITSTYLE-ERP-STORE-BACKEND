package br.com.erp.api.product.domain.entity;

import br.com.erp.api.product.domain.enumerated.ProductStatus;
import br.com.erp.api.product.domain.exception.DuplicateSkuCombinationException;
import br.com.erp.api.product.domain.exception.ProductAlreadyDeactivatedException;
import br.com.erp.api.product.domain.exception.ProductNotReadyForSaleException;
import br.com.erp.api.product.domain.port.SkuUniquenessChecker;
import br.com.erp.api.product.domain.valueobject.Slug;

import java.util.ArrayList;
import java.util.List;

public class Product {

    private Long id;
    private String name;
    private String description;
    private Slug slug;
    private Long categoryId;
    private List<Sku> skus = new ArrayList<>();

    private boolean active;

    protected Product(
            Long id,
            String name,
            String description,
            Slug slug,
            Long categoryId,
            boolean active
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.slug = slug;
        this.categoryId = categoryId;
        this.active = active;
    }


    // Criação
    public Product(
            String name,
            String description,
            Long categoryId
    ) {
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.slug = Slug.fromName(name);
        this.active = false;
    }

    public static Product restore(
            Long id,
            String name,
            String description,
            Slug slug,
            Long categoryId,
            boolean active
    ) {
        return new Product(id, name, description, slug, categoryId, active);
    }

    public ProductStatus getStatus() {
        return ProductStatus.DRAFT;
    }

    public List<Sku> getSkus() {
        return skus;
    }

    public void addSku(Sku sku) {

        sku.attachToProduct(this.id);
        skus.add(sku);
    }

    public void activate() {
        if (getStatus() != ProductStatus.READY_FOR_SALE) {
            throw new ProductNotReadyForSaleException();
        }
        this.active = true;
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

    public void deactivate() {
        if (!this.active) {
            throw new ProductAlreadyDeactivatedException();
        }
        this.active = false;
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

    public boolean isActive() {
        return active;
    }
}

