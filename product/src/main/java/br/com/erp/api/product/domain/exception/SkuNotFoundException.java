package br.com.erp.api.product.domain.exception;

import br.com.erp.api.shared.domain.exception.DomainException;

public class SkuNotFoundException extends DomainException {
    public SkuNotFoundException(Long skuId) {
        super("Sku não encontrado para o ID: " + skuId);
    }
}
