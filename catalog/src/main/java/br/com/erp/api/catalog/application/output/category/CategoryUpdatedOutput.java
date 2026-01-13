package br.com.erp.api.catalog.application.output.category;

public record CategoryUpdatedOutput(
        Long id,
        String name,
        Boolean active
) {

}
