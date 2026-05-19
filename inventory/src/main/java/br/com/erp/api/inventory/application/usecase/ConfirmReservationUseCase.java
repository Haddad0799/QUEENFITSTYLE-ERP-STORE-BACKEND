package br.com.erp.api.inventory.application.usecase;

import br.com.erp.api.inventory.application.port.out.InventoryRepositoryPort;
import br.com.erp.api.inventory.domain.exception.ReservationNotFoundException;
import br.com.erp.api.shared.event.InventoryReservationChangedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ConfirmReservationUseCase {

    private final InventoryRepositoryPort repository;
    private final ApplicationEventPublisher publisher;

    public ConfirmReservationUseCase(InventoryRepositoryPort repository, ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    public void confirm(UUID reservationId) {
        boolean ok = repository.confirmReservation(reservationId);
        if (!ok) throw new ReservationNotFoundException("Reserva não encontrada ou já processada: " + reservationId);

        // tenta resolver o skuId associado para publicar o evento com contexto
        Long skuId = repository.findSkuIdByReservationId(reservationId).orElse(null);
        publisher.publishEvent(new InventoryReservationChangedEvent(reservationId, skuId, "CONFIRMED"));
    }
}

