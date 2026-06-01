package br.com.erp.api.order.application.query;

import br.com.erp.api.order.application.port.out.ImageUrlResolverPort;
import br.com.erp.api.order.application.query.projection.OrderItemRow;
import br.com.erp.api.order.application.query.projection.OrderReservationRow;
import br.com.erp.api.order.domain.entity.Order;
import br.com.erp.api.order.presentation.dto.response.OrderCustomerDTO;
import br.com.erp.api.order.presentation.dto.response.OrderDetailsDTO;
import br.com.erp.api.order.presentation.dto.response.OrderItemDTO;
import br.com.erp.api.order.presentation.dto.response.OrderReservationDTO;
import br.com.erp.api.order.presentation.dto.response.OrderReservationSummaryDTO;
import br.com.erp.api.order.presentation.dto.response.OrderTimelineDTO;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Monta o {@link OrderDetailsDTO} a partir das projeções cruas das queries.
 * Centraliza regras de apresentação (resolução de URL de imagem, cálculo de
 * resumo operacional de reservas) para manter o controller e o repositório enxutos.
 */
@Component
public class OrderDetailsAssembler {

    private static final String RESERVED_STATUS = "RESERVED";

    private final ImageUrlResolverPort imageUrlResolver;

    public OrderDetailsAssembler(ImageUrlResolverPort imageUrlResolver) {
        this.imageUrlResolver = imageUrlResolver;
    }

    public OrderDetailsDTO assemble(Order order,
                                    OrderCustomerDTO customer,
                                    List<OrderItemRow> itemRows,
                                    List<OrderReservationRow> reservationRows,
                                    List<OrderTimelineDTO> timeline) {

        List<OrderItemDTO> items = itemRows.stream()
                .map(this::toItemDTO)
                .toList();

        List<OrderReservationDTO> reservations = reservationRows.stream()
                .map(this::toReservationDTO)
                .toList();

        OrderReservationSummaryDTO summary = buildSummary(reservationRows);

        return new OrderDetailsDTO(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getNotes(),
                order.getWhatsappMessage(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getExpiresAt(),
                order.getConfirmedAt(),
                order.getCancelledAt(),
                customer,
                items,
                reservations,
                summary,
                timeline
        );
    }

    private OrderItemDTO toItemDTO(OrderItemRow row) {
        return new OrderItemDTO(
                row.itemId(),
                row.skuId(),
                row.skuCode(),
                row.productName(),
                row.colorName(),
                row.colorHex(),
                row.sizeLabel(),
                imageUrlResolver.resolve(row.imageKey()),
                row.reservationId(),
                row.quantity(),
                row.unitPrice(),
                row.subtotal()
        );
    }

    private OrderReservationDTO toReservationDTO(OrderReservationRow row) {
        return new OrderReservationDTO(
                row.reservationId(),
                row.skuId(),
                row.skuCode(),
                row.quantity(),
                row.status(),
                row.expiresAt()
        );
    }

    private OrderReservationSummaryDTO buildSummary(List<OrderReservationRow> rows) {
        LocalDateTime now = LocalDateTime.now();

        int totalReservedItems = rows.stream()
                .filter(r -> RESERVED_STATUS.equalsIgnoreCase(r.status()))
                .mapToInt(OrderReservationRow::quantity)
                .sum();

        LocalDateTime earliestExpiry = rows.stream()
                .filter(r -> RESERVED_STATUS.equalsIgnoreCase(r.status()))
                .map(OrderReservationRow::expiresAt)
                .filter(e -> e != null)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        Long expiresInMinutes = earliestExpiry == null
                ? null
                : Math.max(0L, Duration.between(now, earliestExpiry).toMinutes());

        boolean hasExpiredReservations = rows.stream()
                .anyMatch(r -> r.expiresAt() != null && r.expiresAt().isBefore(now));

        return new OrderReservationSummaryDTO(
                totalReservedItems,
                expiresInMinutes,
                hasExpiredReservations
        );
    }
}
