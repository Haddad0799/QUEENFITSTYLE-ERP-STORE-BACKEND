package br.com.erp.api.inventory.domain.exception;

import br.com.erp.api.shared.domain.exception.DomainException;

public class InsufficientStockException extends DomainException {
    public InsufficientStockException(Long skuId, int requested, int available) {
        super("Estoque insuficiente para SKU " + skuId +
                ": solicitado=" + requested + ", disponível=" + available);
    }
}