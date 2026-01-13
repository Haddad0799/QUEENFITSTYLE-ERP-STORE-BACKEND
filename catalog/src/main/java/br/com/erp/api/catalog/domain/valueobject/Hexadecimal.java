package br.com.erp.api.catalog.domain.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

public record Hexadecimal(String value) {

    private static final Pattern HEX_PATTERN = Pattern.compile("^#([A-Fa-f0-9]{6})$");

    public Hexadecimal {
        Objects.requireNonNull(value, "Hex color cannot be null");

        if (!HEX_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid hex color: " + value);
        }

        value = value.toUpperCase();
    }
}
