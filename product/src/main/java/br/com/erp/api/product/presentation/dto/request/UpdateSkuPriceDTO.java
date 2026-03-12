package br.com.erp.api.product.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public record UpdateSkuPriceDTO(

        @DecimalMin(value = "0.01", message = "O preço de custo deve ser maior que zero")
        BigDecimal costPrice,

        @DecimalMin(value = "0.01", message = "O preço de venda deve ser maior que zero")
        BigDecimal sellingPrice
) {}