package br.com.erp.api.catalog.application.output;

public record CategoryOutput(
        Long id,
        String name,
        Boolean active
) {
}
