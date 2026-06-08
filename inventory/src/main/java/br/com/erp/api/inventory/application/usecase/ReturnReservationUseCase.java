package br.com.erp.api.inventory.application.usecase;

import br.com.erp.api.inventory.application.port.out.InventoryRepositoryPort;
import br.com.erp.api.inventory.domain.exception.ReservationNotFoundException;
import br.com.erp.api.shared.event.InventoryReservationChangedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Devolve ao estoque uma reserva já CONFIRMED (pedido pago/entregue que foi devolvido).
 * Diferente de {@link ReleaseReservationUseCase}, que só serve para reservas ainda RESERVED:
 * aqui o estoque físico já havia sido consumido, então a quantidade é reincrementada.
 */
@Service
public class ReturnReservationUseCase {

    private final InventoryRepositoryPort repository;
    private final ApplicationEventPublisher publisher;

    public ReturnReservationUseCase(InventoryRepositoryPort repository, ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    public void returnStock(UUID reservationId) {
        boolean ok = repository.returnReservation(reservationId);
        if (!ok) throw new ReservationNotFoundException("Reserva não encontrada ou não estava confirmada: " + reservationId);

        Long skuId = repository.findSkuIdByReservationId(reservationId).orElse(null);
        publisher.publishEvent(new InventoryReservationChangedEvent(reservationId, skuId, "RETURNED"));
    }
}
