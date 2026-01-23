package br.com.erp.api.product.domain.valueobject;

import java.text.Normalizer;

public record Slug(String value) {

    public Slug {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Slug não pode ser nulo ou vazio");
        }
    }

    public static Slug fromName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nome inválido para slug");
        }

        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");

        String slugified = normalized.toLowerCase()
                .replaceAll("[\\s]+", "-")
                .replaceAll("[^a-z0-9\\-]", "")
                .replaceAll("(^-|-$)", "");

        return new Slug(slugified);
    }

    public static Slug fromValue(String value) {
        return new Slug(value);
    }
}
