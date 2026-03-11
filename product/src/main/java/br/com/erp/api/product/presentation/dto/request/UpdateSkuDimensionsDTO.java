package br.com.erp.api.product.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;

public record UpdateSkuDimensionsDTO(
        @DecimalMin(value = "0.1", message = "Largura deve ser maior que zero")
        @Digits(integer = 6, fraction = 2, message = "Largura inválida")
        BigDecimal width,

        @DecimalMin(value = "0.1", message = "Altura deve ser maior que zero")
        @Digits(integer = 6, fraction = 2, message = "Altura inválida")
        BigDecimal height,

        @DecimalMin(value = "0.1", message = "Comprimento deve ser maior que zero")
        @Digits(integer = 6, fraction = 2, message = "Comprimento inválido")
        BigDecimal length,

        @DecimalMin(value = "0.001", message = "Peso deve ser maior que zero")
        @Digits(integer = 4, fraction = 3, message = "Peso inválido")
        BigDecimal weight
) {}