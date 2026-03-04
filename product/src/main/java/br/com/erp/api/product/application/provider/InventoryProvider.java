package br.com.erp.api.product.application.provider;

import br.com.erp.api.product.presentation.dto.response.SkuStock;

public interface InventoryProvider {
    SkuStock getBySkuId(Long skuId);
}
