package br.com.erp.api.order.presentation.controller;

import br.com.erp.api.order.application.query.OrderAdminQueryService;
import br.com.erp.api.order.application.query.filter.OrderAdminFilter;
import br.com.erp.api.order.application.usecase.CancelOrderUseCase;
import br.com.erp.api.order.application.usecase.ConfirmOrderUseCase;
import br.com.erp.api.order.application.usecase.ExpireOrderUseCase;
import br.com.erp.api.order.application.usecase.MarkOrderDeliveredUseCase;
import br.com.erp.api.order.application.usecase.MarkOrderReturnedUseCase;
import br.com.erp.api.order.domain.entity.Order;
import br.com.erp.api.order.domain.enumerated.OrderStatus;
import br.com.erp.api.order.presentation.dto.request.CancelOrderRequest;
import br.com.erp.api.order.presentation.dto.response.OrderActionResponse;
import br.com.erp.api.order.presentation.dto.response.OrderDetailsDTO;
import br.com.erp.api.order.presentation.dto.response.OrderSummaryDTO;
import br.com.erp.api.order.presentation.dto.response.OrderTimelineDTO;
import br.com.erp.api.order.presentation.dto.response.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/erp/orders")
public class OrderAdminController {

    private static final int    MAX_PAGE_SIZE   = 100;
    private static final String DEFAULT_ACTOR   = "admin";
    private static final String SYSTEM_ACTOR    = "system";

    private final OrderAdminQueryService    queryService;
    private final ConfirmOrderUseCase       confirmOrderUseCase;
    private final CancelOrderUseCase        cancelOrderUseCase;
    private final ExpireOrderUseCase        expireOrderUseCase;
    private final MarkOrderDeliveredUseCase markOrderDeliveredUseCase;
    private final MarkOrderReturnedUseCase  markOrderReturnedUseCase;

    public OrderAdminController(OrderAdminQueryService queryService,
                                ConfirmOrderUseCase confirmOrderUseCase,
                                CancelOrderUseCase cancelOrderUseCase,
                                ExpireOrderUseCase expireOrderUseCase,
                                MarkOrderDeliveredUseCase markOrderDeliveredUseCase,
                                MarkOrderReturnedUseCase markOrderReturnedUseCase) {
        this.queryService              = queryService;
        this.confirmOrderUseCase       = confirmOrderUseCase;
        this.cancelOrderUseCase        = cancelOrderUseCase;
        this.expireOrderUseCase        = expireOrderUseCase;
        this.markOrderDeliveredUseCase = markOrderDeliveredUseCase;
        this.markOrderReturnedUseCase  = markOrderReturnedUseCase;
    }

    @GetMapping
    public ResponseEntity<PageResponse<OrderSummaryDTO>> list(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) LocalDate createdAtFrom,
            @RequestParam(required = false) LocalDate createdAtTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.clamp(size, 1, MAX_PAGE_SIZE)
        );

        OrderAdminFilter filter = new OrderAdminFilter(
                status, customerName, createdAtFrom, createdAtTo
        );

        Page<OrderSummaryDTO> result = queryService.findAll(filter, pageable);
        return ResponseEntity.ok(PageResponse.from(result));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailsDTO> details(@PathVariable Long orderId) {
        return ResponseEntity.ok(queryService.findById(orderId));
    }

    @GetMapping("/{orderId}/timeline")
    public ResponseEntity<List<OrderTimelineDTO>> timeline(@PathVariable Long orderId) {
        return ResponseEntity.ok(queryService.findTimeline(orderId));
    }

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<OrderActionResponse> confirm(
            @PathVariable Long orderId,
            @RequestHeader(name = "X-Actor", required = false) String actor
    ) {
        Order order = confirmOrderUseCase.execute(orderId, resolveActor(actor));
        return ResponseEntity.ok(OrderActionResponse.from(order));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderActionResponse> cancel(
            @PathVariable Long orderId,
            @Valid @RequestBody(required = false) CancelOrderRequest request,
            @RequestHeader(name = "X-Actor", required = false) String actor
    ) {
        String reason = request != null ? request.reason() : null;
        Order order = cancelOrderUseCase.execute(orderId, reason, resolveActor(actor));
        return ResponseEntity.ok(OrderActionResponse.from(order));
    }

    @PostMapping("/{orderId}/expire")
    public ResponseEntity<OrderActionResponse> expire(
            @PathVariable Long orderId,
            @RequestHeader(name = "X-Actor", required = false) String actor
    ) {
        Order order = expireOrderUseCase.execute(orderId,
                actor != null && !actor.isBlank() ? actor : SYSTEM_ACTOR);
        return ResponseEntity.ok(OrderActionResponse.from(order));
    }

    @PostMapping("/{orderId}/deliver")
    public ResponseEntity<OrderActionResponse> deliver(
            @PathVariable Long orderId,
            @RequestHeader(name = "X-Actor", required = false) String actor
    ) {
        Order order = markOrderDeliveredUseCase.execute(orderId, resolveActor(actor));
        return ResponseEntity.ok(OrderActionResponse.from(order));
    }

    @PostMapping("/{orderId}/return")
    public ResponseEntity<OrderActionResponse> returnOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody(required = false) CancelOrderRequest request,
            @RequestHeader(name = "X-Actor", required = false) String actor
    ) {
        String reason = request != null ? request.reason() : null;
        Order order = markOrderReturnedUseCase.execute(orderId, reason, resolveActor(actor));
        return ResponseEntity.ok(OrderActionResponse.from(order));
    }

    private String resolveActor(String actor) {
        return (actor != null && !actor.isBlank()) ? actor : DEFAULT_ACTOR;
    }
}
