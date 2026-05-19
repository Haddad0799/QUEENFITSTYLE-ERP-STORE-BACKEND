package br.com.erp.api.inventory.application.usecase;

import br.com.erp.api.inventory.application.port.out.InventoryRepositoryPort;
import br.com.erp.api.shared.event.InventoryReservationChangedEvent;
import org.springframework.context.ApplicationEventPublisher;
import br.com.erp.api.inventory.domain.exception.InsufficientStockException;
import br.com.erp.api.inventory.domain.exception.SkuNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ReserveStockUseCase {

    private final InventoryRepositoryPort repository;
    private final ApplicationEventPublisher publisher;

    public ReserveStockUseCase(InventoryRepositoryPort repository, ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    public UUID reserve(String skuCode, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantidade deve ser positiva");

        Long skuId = repository.findSkuIdByCode(skuCode)
                .orElseThrow(() -> new SkuNotFoundException(skuCode));

        UUID id = UUID.randomUUID();

        boolean ok = repository.tryReserve(skuId, quantity, id);
        if (!ok) {
            throw new InsufficientStockException(skuId, quantity, repository.findBySkuId(skuId)
                    .map(s -> s.getQuantity() - s.getReserved()).orElse(0));
        }

        publisher.publishEvent(new InventoryReservationChangedEvent(id, skuId, "RESERVED"));

        return id;
    }
}

