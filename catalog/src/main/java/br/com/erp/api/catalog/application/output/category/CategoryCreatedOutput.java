package br.com.erp.api.catalog.application.output.category;

public record CategoryCreatedOutput(
        Long id,
        String name,
        Boolean active
) {
}
