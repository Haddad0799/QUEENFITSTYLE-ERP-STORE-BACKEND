package br.com.erp.api.attribute.domain.valueobject;

import br.com.erp.api.attribute.domain.exception.category.InvalidNameException;

import java.text.Normalizer;

public record CategoryName(
        String displayName,
        String normalizedName
) {

    public CategoryName(String raw) {
        this(validateAndTrim(raw), normalize(raw));
    }

    private static String validateAndTrim(String raw) {
        if (raw == null) {
            throw new InvalidNameException("Nome não pode ser nulo");
        }

        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            throw new InvalidNameException("Nome não pode ser vazio");
        }

        return trimmed;
    }

    private static String normalize(String input) {
        String noAccents = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        String normalized = noAccents
                .toLowerCase()
                .replaceAll("[0-9]", "")
                .replaceAll("[^a-z]+", "-")
                .replaceAll("(^-+|-+$)", "");

        if (normalized.isBlank()) {
            throw new InvalidNameException("Nome inválido para normalização");
        }

        return normalized;
    }
}
