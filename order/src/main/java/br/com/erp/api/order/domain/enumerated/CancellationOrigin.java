package br.com.erp.api.order.domain.enumerated;

/**
 * Origem de um cancelamento de pedido. Distingue quem disparou a ação para que efeitos
 * colaterais possam reagir de forma diferente — em especial, avisar a dona da loja apenas
 * quando o cancelamento parte da cliente (e-commerce), e não da própria vendedora (ERP).
 *
 * A origem é definida pelo papel autenticado da requisição de cancelamento (resolvida no
 * controller): token de serviço (ROLE_SERVICE), usado pelo e-commerce, mapeia para
 * {@link #CUSTOMER}; usuário ADMIN, a vendedora no ERP, mapeia para {@link #ADMIN}.
 */
public enum CancellationOrigin {

    CUSTOMER,
    ADMIN;

    /** Rótulo de actor na timeline para cancelamento feito pela cliente (e-commerce). */
    public static final String CUSTOMER_ACTOR = "customer";
}
