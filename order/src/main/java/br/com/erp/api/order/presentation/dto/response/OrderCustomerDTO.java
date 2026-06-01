package br.com.erp.api.order.presentation.dto.response;

public record OrderCustomerDTO(
        Long customerId,
        String name,
        String phone,
        String city
) {}
