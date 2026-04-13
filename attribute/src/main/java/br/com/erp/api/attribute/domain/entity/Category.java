package br.com.erp.api.attribute.domain.entity;

import br.com.erp.api.attribute.domain.exception.category.CategoryAlreadyActiveException;
import br.com.erp.api.attribute.domain.exception.category.CategoryAlreadyInactiveException;
import br.com.erp.api.attribute.domain.exception.category.CategoryNameAlreadyUsedException;
import br.com.erp.api.attribute.domain.valueobject.CategoryName;

public class Category {
    private Long id;
    private CategoryName name;
    private Boolean active;
    private Long parentId;

    public Category(CategoryName name) {
        this.name = name;
        this.active = true;
        this.parentId = null;
    }

    public Category(CategoryName name, Long parentId) {
        this.name = name;
        this.active = true;
        this.parentId = parentId;
    }

    public Category(Long id, CategoryName name, Boolean active) {
        this.id = id;
        this.name = name;
        this.active = active;
        this.parentId = null;
    }

    public Category(Long id, CategoryName name, Boolean active, Long parentId) {
        this.id = id;
        this.name = name;
        this.active = active;
        this.parentId = parentId;
    }

    public String getNormalizedName() {
        return this.name.normalizedName();
    }

    public String getDisplayName(){
        return this.name.displayName();
    }

    public boolean isSubcategory() {
        return this.parentId != null;
    }

    public boolean isParent() {
        return this.parentId == null;
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

    public Long getParentId() {
        return this.parentId;
    }
}
