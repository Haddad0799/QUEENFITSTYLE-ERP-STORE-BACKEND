package br.com.erp.api.order.domain.exception;

import br.com.erp.api.shared.domain.exception.DomainException;

public class SkuWithoutPriceException extends DomainException {

    public SkuWithoutPriceException(Long skuId) {
        super("SKU %d sem preço cadastrado — configure o preço antes de finalizar o pedido".formatted(skuId));
    }
}
