package br.com.erp.api.inventory.application.port.out;

import br.com.erp.api.inventory.domain.entity.SkuStock;

import java.util.Optional;
import java.util.UUID;

public interface InventoryRepositoryPort {

    Optional<SkuStock> findBySkuId(Long skuId);

    /**
     * Tenta reservar uma quantidade de forma atômica. Retorna true se a reserva for criada.
     */
    boolean tryReserve(Long skuId, int qty, UUID reservationId);

    /**
     * Confirma uma reserva (consome estoque). Retorna true se confirmada.
     */
    boolean confirmReservation(UUID reservationId);

    /**
     * Libera / cancela uma reserva. Retorna true se liberada.
     */
    boolean releaseReservation(UUID reservationId);

    /**
     * Retorna o skuId associado a uma reserva (se existir).
     */
    java.util.Optional<Long> findSkuIdByReservationId(UUID reservationId);

    /**
     * Retorna o skuId a partir do sku_code (tabela skus).
     */
    java.util.Optional<Long> findSkuIdByCode(String skuCode);
    /**
     * Ajusta a quantidade de estoque (admin).
     */
    void adjustStock(Long skuId, int newQuantity, String reason);
}

