package br.com.erp.api.product.application.dto;

import java.util.Map;
import java.util.Set;

/**
 * Resultado do batch insert de SKUs com diferenciação entre novos e existentes.
 *
 * @param allSkuCodeToId mapa de TODOS os sku_codes → ids (novos + já existentes)
 * @param newSkuCodes    conjunto de sku_codes que foram realmente CRIADOS neste batch
 */
public record SkuBatchResult(
        Map<String, Long> allSkuCodeToId,
        Set<String> newSkuCodes
) {

    public boolean isNew(String skuCode) {
        return newSkuCodes.contains(skuCode);
    }

    public int totalCreated() {
        return newSkuCodes.size();
    }

    public int totalIgnored() {
        return allSkuCodeToId.size() - newSkuCodes.size();
    }
}

