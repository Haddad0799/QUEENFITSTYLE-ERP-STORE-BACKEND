package br.com.erp.api.order.application.usecase;

import br.com.erp.api.order.application.port.out.ReservationDetail;
import br.com.erp.api.order.application.port.out.ReservationValidationPort;
import br.com.erp.api.order.application.port.out.SkuPricingPort;
import br.com.erp.api.order.application.service.WhatsAppUrlService;
import br.com.erp.api.order.domain.entity.Customer;
import br.com.erp.api.order.domain.entity.Order;
import br.com.erp.api.order.domain.entity.OrderItem;
import br.com.erp.api.order.domain.exception.InvalidReservationException;
import br.com.erp.api.order.domain.exception.ReservationAlreadyBoundException;
import br.com.erp.api.order.domain.exception.SkuWithoutPriceException;
import br.com.erp.api.order.domain.port.CustomerRepositoryPort;
import br.com.erp.api.order.domain.port.OrderRepositoryPort;
import br.com.erp.api.order.presentation.dto.request.CreateOrderRequest;
import br.com.erp.api.order.presentation.dto.response.CreateOrderResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CreateOrderUseCase {

    private static final int ORDER_EXPIRATION_HOURS = 24;

    private final ReservationValidationPort reservationValidationPort;
    private final SkuPricingPort pricingPort;
    private final CustomerRepositoryPort customerRepository;
    private final OrderRepositoryPort orderRepository;
    private final WhatsAppUrlService whatsAppUrlService;

    public CreateOrderUseCase(ReservationValidationPort reservationValidationPort,
                              SkuPricingPort pricingPort,
                              CustomerRepositoryPort customerRepository,
                              OrderRepositoryPort orderRepository,
                              WhatsAppUrlService whatsAppUrlService) {
        this.reservationValidationPort = reservationValidationPort;
        this.pricingPort               = pricingPort;
        this.customerRepository        = customerRepository;
        this.orderRepository           = orderRepository;
        this.whatsAppUrlService        = whatsAppUrlService;
    }

    public CreateOrderResponse execute(CreateOrderRequest request) {
        List<UUID> reservationIds = request.reservations().stream()
                .map(UUID::fromString)
                .toList();

        // 1. Busca dados das reservas com JOIN em skus/products (uma única query, sem N+1)
        List<ReservationDetail> details = reservationValidationPort.fetchDetails(reservationIds);

        // 2. Valida: existência, status RESERVED, não expirada
        validateReservations(reservationIds, details);

        // 3. Snapshot de preços no momento do checkout
        List<Long> skuIds = details.stream().map(ReservationDetail::skuId).distinct().toList();
        Map<Long, BigDecimal> prices = pricingPort.findSellingPrices(skuIds);

        // 4. Monta itens
        List<OrderItem> items = details.stream()
                .map(d -> new OrderItem(
                        d.skuId(),
                        d.reservationId(),
                        d.productName(),
                        d.skuCode(),
                        d.colorName(),
                        d.sizeLabel(),
                        d.quantity(),
                        resolvePrice(d.skuId(), prices)
                ))
                .toList();

        BigDecimal total = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. Upsert cliente por telefone — idempotente, atualiza nome/cidade se já existir
        Customer customer = customerRepository.upsertByPhone(
                new Customer(
                        request.customer().name(),
                        request.customer().phone(),
                        request.customer().city()
                )
        );

        // 6. Persiste pedido + itens em uma única transação JDBI
        Order order = orderRepository.saveWithItems(
                Order.create(
                        customer.getId(),
                        total,
                        request.notes(),
                        LocalDateTime.now().plusHours(ORDER_EXPIRATION_HOURS),
                        items
                )
        );

        // 7. Gera mensagem e URL do WhatsApp (requer ID do pedido já persistido)
        String message = whatsAppUrlService.buildMessageText(order, customer);
        String url     = whatsAppUrlService.buildUrl(order, customer);

        order.attachWhatsappMessage(message);
        orderRepository.updateWhatsappMessage(order.getId(), message);

        return new CreateOrderResponse(order.getId(), order.getStatus(), url);
    }

    private void validateReservations(List<UUID> requested, List<ReservationDetail> fetched) {
        Map<UUID, ReservationDetail> byId = fetched.stream()
                .collect(Collectors.toMap(ReservationDetail::reservationId, d -> d));

        for (UUID id : requested) {
            ReservationDetail detail = byId.get(id);
            if (detail == null) {
                throw new InvalidReservationException(id, "não encontrada");
            }
            if (!"RESERVED".equals(detail.status())) {
                throw new ReservationAlreadyBoundException(id);
            }
            if (detail.expiresAt() != null && LocalDateTime.now().isAfter(detail.expiresAt())) {
                throw new InvalidReservationException(id, "expirada");
            }
        }
    }

    private BigDecimal resolvePrice(Long skuId, Map<Long, BigDecimal> prices) {
        BigDecimal price = prices.get(skuId);
        if (price == null) throw new SkuWithoutPriceException(skuId);
        return price;
    }
}
