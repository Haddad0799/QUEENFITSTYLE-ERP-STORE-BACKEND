package br.com.erp.api.product.application.provider;

import br.com.erp.api.product.presentation.dto.response.SkuPriceDTO;

public interface PriceProvider {
    SkuPriceDTO getBySkuId(Long id);
}
