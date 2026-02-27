package br.com.erp.api.product.domain.valueobject;

import br.com.erp.api.product.domain.exception.InvalidSkuCodeException;

import java.util.regex.Pattern;

public record SkuCode(String value) {

    private static final Pattern PATTERN =
            Pattern.compile("^[A-Z0-9]+(-[A-Z0-9]+)*$");

    public SkuCode {
        if (value == null || value.isBlank()) {
            throw new InvalidSkuCodeException("SKU obrigatório");
        }

        value = value.trim().toUpperCase();

        if (value.length() < 3) {
            throw new InvalidSkuCodeException("SKU muito curto, mínimo 3 caracteres");
        }

        if (value.length() > 50) {
            throw new InvalidSkuCodeException("SKU não pode ter mais de 50 caracteres");
        }

        if (!PATTERN.matcher(value).matches()) {
            throw new InvalidSkuCodeException(
                    "SKU deve conter apenas letras e números, separados por hífen"
            );
        }
    }

    public static SkuCode of(String raw) {
        return new SkuCode(raw);
    }
}