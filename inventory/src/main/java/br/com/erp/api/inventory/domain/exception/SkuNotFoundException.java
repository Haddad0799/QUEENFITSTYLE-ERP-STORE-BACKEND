package br.com.erp.api.inventory.domain.exception;

public class SkuNotFoundException extends RuntimeException {
    public SkuNotFoundException(String skuCode) {
        super("SKU não encontrado: " + skuCode);
    }
}