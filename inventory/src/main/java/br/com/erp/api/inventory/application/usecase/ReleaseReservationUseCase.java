package br.com.erp.api.inventory.application.usecase;

import br.com.erp.api.inventory.application.port.out.InventoryRepositoryPort;
import br.com.erp.api.inventory.domain.exception.ReservationNotFoundException;
import br.com.erp.api.shared.event.InventoryReservationChangedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ReleaseReservationUseCase {

    private final InventoryRepositoryPort repository;
    private final ApplicationEventPublisher publisher;

    public ReleaseReservationUseCase(InventoryRepositoryPort repository, ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    public void release(UUID reservationId) {
        boolean ok = repository.releaseReservation(reservationId);
        if (!ok) throw new ReservationNotFoundException("Reserva não encontrada ou já processada: " + reservationId);

        Long skuId = repository.findSkuIdByReservationId(reservationId).orElse(null);
        publisher.publishEvent(new InventoryReservationChangedEvent(reservationId, skuId, "RELEASED"));
    }
}

