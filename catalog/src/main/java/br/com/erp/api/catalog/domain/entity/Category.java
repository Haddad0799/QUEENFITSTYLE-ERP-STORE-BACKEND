package br.com.erp.api.catalog.domain.entity;

import br.com.erp.api.catalog.domain.exception.category.CategoryAlreadyActiveException;
import br.com.erp.api.catalog.domain.exception.category.CategoryAlreadyInactiveException;
import br.com.erp.api.catalog.domain.exception.category.CategoryNameAlreadyUsedException;
import br.com.erp.api.catalog.domain.valueobject.CategoryName;

public class Category {
    private Long id;
    private CategoryName name;
    private Boolean active;

    public Category(CategoryName name) {
        this.name = name;
        this.active = true;
    }

    public Category(Long id, CategoryName name, Boolean active) {
        this.id = id;
        this.name = name;
        this.active = active;
    }

    public String getNormalizedName() {
        return this.name.normalizedName();
    }

    public String getDisplayName(){
        return this.name.displayName();
    }

    public void deactivate() {
        if (!this.active) {
            throw new CategoryAlreadyInactiveException();
        }
        this.active = false;
    }

    public void activate() {
        if (this.active) {
            throw new CategoryAlreadyActiveException();
        }
        this.active = true;
    }


    public void rename(CategoryName newName) {

        if (this.name.normalizedName().equals(newName.normalizedName())) {
            throw new CategoryNameAlreadyUsedException();
        }

        this.name = newName;
    }


    public Long getId() {
        return this.id;
    }

    public Boolean isActive() {
        return this.active;
    }
}
