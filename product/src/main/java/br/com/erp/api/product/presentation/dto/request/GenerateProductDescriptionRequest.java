package br.com.erp.api.product.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record GenerateProductDescriptionRequest(
        @NotBlank(message = "Nome do produto é obrigatório")
        String productName,

        @NotBlank(message = "Categoria é obrigatória")
        String categoryName,

        String subcategoryName,
        String brand,
        String material,
        String color,
        String fit,
        String targetAudience,

        @Size(max = 8, message = "Informe no máximo 8 destaques")
        List<@NotBlank(message = "Destaques não podem ser vazios") String> highlights,

        String additionalDetails
) {
}
