package br.com.erp.api.product.application.dto;

import java.util.List;

public record ImportResult(
        int totalProducts,
        int successProducts,
        List<String>errors
) {
}
