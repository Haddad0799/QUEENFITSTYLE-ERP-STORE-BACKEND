package br.com.erp.api.pricing.domain.exception;

import br.com.erp.api.shared.domain.exception.DomainException;

public class SkuPriceNotFoundException extends DomainException {

    public SkuPriceNotFoundException(Long skuId) {
        super(String.format("Preço do SKU %d não encontrado", skuId));
    }
}
