package br.com.erp.api.catalog.domain.valueobject;

import java.util.Objects;

public record SizeLabel(String value) {

    public SizeLabel {
        Objects.requireNonNull(value, "Size cannot be null");

        value = value.trim().toUpperCase();

        if (value.isEmpty()) {
            throw new IllegalArgumentException("Size cannot be empty");
        }
    }
}
