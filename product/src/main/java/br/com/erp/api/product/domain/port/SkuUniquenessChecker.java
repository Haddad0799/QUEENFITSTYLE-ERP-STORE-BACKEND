package br.com.erp.api.product.domain.port;

import java.util.List;
import java.util.Map;

public interface SkuUniquenessChecker {

    /**
     * Retorna as combinações (colorId, sizeId) que já existem para o produto.
     *
     * @param productId id do produto
     * @param combos lista de combinações (colorId, sizeId) para verificar
     * @return lista de combinações existentes
     */
    List<Map.Entry<Long, Long>> existsBatch(Long productId, List<Map.Entry<Long, Long>> combos);
}
