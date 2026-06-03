package br.com.erp.api.order.presentation.dto.response;

/**
 * Endereço de entrega exibido para o admin saber onde entregar o pedido.
 */
public record OrderDeliveryAddressDTO(
        String cep,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state
) {}
