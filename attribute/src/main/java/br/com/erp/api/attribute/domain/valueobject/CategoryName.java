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
        // Remove acentos
        String noAccents = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Remove números
        noAccents = noAccents.replaceAll("[0-9]", "");

        // Substitui espaços por _
        noAccents = noAccents.replaceAll("\\s+", "_");

        // Remove caracteres especiais (mantém letras e _)
        noAccents = noAccents.replaceAll("[^a-zA-Z_]", "");

        // Tudo em maiúsculo
        return noAccents.toUpperCase();
    }
}

