package br.com.erp.api.order.domain.enumerated;

/**
 * Origem de um cancelamento de pedido. Distingue quem disparou a ação para que efeitos
 * colaterais possam reagir de forma diferente — em especial, avisar a dona da loja apenas
 * quando o cancelamento parte da cliente (e-commerce), e não da própria vendedora (ERP).
 *
 * A distinção reaproveita o {@code actor} já registrado na timeline: o e-commerce envia
 * {@link #CUSTOMER_ACTOR} no cabeçalho {@code X-Actor} ao cancelar em nome da cliente;
 * qualquer outro valor (inclusive o default {@code "admin"} da área administrativa) é
 * tratado como cancelamento da vendedora.
 */
public enum CancellationOrigin {

    CUSTOMER,
    ADMIN;

    /** Valor de {@code X-Actor} enviado pelo e-commerce ao cancelar em nome da cliente. */
    public static final String CUSTOMER_ACTOR = "customer";

    public static CancellationOrigin fromActor(String actor) {
        return CUSTOMER_ACTOR.equalsIgnoreCase(actor) ? CUSTOMER : ADMIN;
    }
}
