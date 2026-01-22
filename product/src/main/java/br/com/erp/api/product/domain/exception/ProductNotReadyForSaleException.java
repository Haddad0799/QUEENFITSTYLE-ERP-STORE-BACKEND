package br.com.erp.api.product.domain.exception;

import br.com.erp.api.shared.domain.exception.DomainException;

public class ProductNotReadyForSaleException extends DomainException {

    public ProductNotReadyForSaleException() {
        super("Produto não está pronto para venda");
    }
}
