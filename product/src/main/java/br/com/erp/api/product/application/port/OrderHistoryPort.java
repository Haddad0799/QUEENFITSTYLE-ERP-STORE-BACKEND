package br.com.erp.api.product.application.port;

import java.util.Collection;
import java.util.Set;

/**
 * Porta de saída para consultar vínculos históricos com pedidos.
 * Permite que o módulo de produto decida entre exclusão física e lógica
 * sem depender diretamente do módulo de pedidos.
 */
public interface OrderHistoryPort {

    /**
     * Indica se algum SKU do produto já foi vinculado a um item de pedido,
     * em qualquer status de pedido.
     */
    boolean existsByProductId(Long productId);

    /**
     * Dentre os SKUs informados, retorna os que possuem vínculo com itens de pedido.
     */
    Set<Long> findSkuIdsWithOrders(Collection<Long> skuIds);
}
