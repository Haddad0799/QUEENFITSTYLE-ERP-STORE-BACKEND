package br.com.erp.api.order.application.query.filter;

import br.com.erp.api.order.domain.enumerated.OrderStatus;

import java.time.LocalDate;

public record OrderAdminFilter(
        OrderStatus status,
        String customerName,
        LocalDate createdAtFrom,
        LocalDate createdAtTo
) {
    public boolean hasStatus()        { return status != null; }
    public boolean hasCustomerName()  { return customerName != null && !customerName.isBlank(); }
    public boolean hasCreatedAtFrom() { return createdAtFrom != null; }
    public boolean hasCreatedAtTo()   { return createdAtTo != null; }
}
