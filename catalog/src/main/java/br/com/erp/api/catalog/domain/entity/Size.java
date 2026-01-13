package br.com.erp.api.catalog.domain.entity;

import br.com.erp.api.catalog.domain.valueobject.SizeLabel;

public class Size {
    private Long id;
    private final SizeLabel label;
    private boolean active;

    public Size(SizeLabel label) {
        this.label = label;
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public String getLabel() {
        return label.value();
    }

    public boolean isActive() {
        return active;
    }
}
