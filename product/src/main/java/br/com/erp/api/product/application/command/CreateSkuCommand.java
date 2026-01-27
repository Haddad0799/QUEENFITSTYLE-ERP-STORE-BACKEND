package br.com.erp.api.product.application.command;

import java.util.List;

public record CreateSkuCommand(
        Long productId,
        List<SkuData> skus
) {}

