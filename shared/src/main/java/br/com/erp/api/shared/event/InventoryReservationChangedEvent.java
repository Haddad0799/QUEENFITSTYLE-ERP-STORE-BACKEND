package br.com.erp.api.shared.event;

import java.util.UUID;

/**
 * Evento publicado pelo módulo de inventory sempre que uma reserva muda de estado
 * (RESERVED, CONFIRMED, RELEASED). Colocado em shared para ser consumido por outros módulos.
 */
public class InventoryReservationChangedEvent {

    private final UUID reservationId;
    private final Long skuId; // pode ser null se não conhecido
    private final String action; // RESERVED | CONFIRMED | RELEASED

    public InventoryReservationChangedEvent(UUID reservationId, Long skuId, String action) {
        this.reservationId = reservationId;
        this.skuId = skuId;
        this.action = action;
    }

    public UUID reservationId() { return reservationId; }
    public Long skuId() { return skuId; }
    public String action() { return action; }
}

