package br.com.erp.api.attribute.application.output;

public record CategoryOutput(
        Long id,
        String name,
        String normalizedName,
        Boolean active,
        Long parentId
) {
}
