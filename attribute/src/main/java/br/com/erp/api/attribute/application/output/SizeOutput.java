package br.com.erp.api.attribute.application.output;

public record SizeOutput(
        Long id,
        String label,
        String type,
        int displayOrder
) {
}

