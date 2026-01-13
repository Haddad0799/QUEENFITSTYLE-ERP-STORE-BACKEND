package br.com.erp.api.catalog.domain.entity;

import br.com.erp.api.catalog.domain.valueobject.Hexadecimal;
import br.com.erp.api.catalog.domain.valueobject.Name;

public class Color {
    private Long id;
    private final Name name;
    private final Hexadecimal hexaCode;
    private Boolean active;

    public Color(Long id, Name name, Hexadecimal hexaCode, Boolean active) {
        this.id = id;
        this.name = name;
        this.hexaCode = hexaCode;
        this.active = active;
    }

    public Color(Name name, Hexadecimal hexaCode) {
        this.name = name;
        this.hexaCode = hexaCode;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public String getNormalizedName() {
        return this.name.normalizedName();
    }

    public String getDisplayName(){
        return this.name.displayName();
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }


    public String getHexaCode() {
        return this.hexaCode.value();
    }
}
