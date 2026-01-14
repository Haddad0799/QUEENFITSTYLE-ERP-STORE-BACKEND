package br.com.erp.api.catalog.presentation.dto.size;

public record SizeAdminDetailsDTO(
        Long id,
        String etiqueta,
        String tipo,
        int ordem
) {
}
