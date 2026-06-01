package br.com.erp.api.order.application.query;

import br.com.erp.api.order.application.query.filter.OrderAdminFilter;
import br.com.erp.api.order.application.query.projection.OrderItemRow;
import br.com.erp.api.order.application.query.projection.OrderReservationRow;
import br.com.erp.api.order.domain.entity.Order;
import br.com.erp.api.order.domain.exception.OrderNotFoundException;
import br.com.erp.api.order.domain.port.OrderRepositoryPort;
import br.com.erp.api.order.presentation.dto.response.OrderCustomerDTO;
import br.com.erp.api.order.presentation.dto.response.OrderDetailsDTO;
import br.com.erp.api.order.presentation.dto.response.OrderSummaryDTO;
import br.com.erp.api.order.presentation.dto.response.OrderTimelineDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderAdminQueryService {

    private final OrderAdminQueryRepository repository;
    private final OrderRepositoryPort       orderRepository;
    private final OrderDetailsAssembler     assembler;

    public OrderAdminQueryService(OrderAdminQueryRepository repository,
                                  OrderRepositoryPort orderRepository,
                                  OrderDetailsAssembler assembler) {
        this.repository      = repository;
        this.orderRepository = orderRepository;
        this.assembler       = assembler;
    }

    public Page<OrderSummaryDTO> findAll(OrderAdminFilter filter, Pageable pageable) {
        return repository.findAll(filter, pageable);
    }

    public OrderDetailsDTO findById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        OrderCustomerDTO customer = repository.findCustomerByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        List<OrderItemRow>        itemRows        = repository.findItemsByOrderId(orderId);
        List<OrderReservationRow> reservationRows = repository.findReservationsByOrderId(orderId);
        List<OrderTimelineDTO>    timeline        = repository.findTimelineByOrderId(orderId);

        return assembler.assemble(order, customer, itemRows, reservationRows, timeline);
    }

    public List<OrderTimelineDTO> findTimeline(Long orderId) {
        if (orderRepository.findById(orderId).isEmpty()) {
            throw new OrderNotFoundException(orderId);
        }
        return repository.findTimelineByOrderId(orderId);
    }
}
