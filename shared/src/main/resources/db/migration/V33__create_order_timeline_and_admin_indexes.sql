-- Habilita pg_trgm para busca eficiente em customers.name (LIKE %x%)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Timeline de eventos do pedido (append-only). Cada transição operacional
-- registra um evento aqui, preservando histórico para o painel administrativo.
CREATE TABLE order_timeline (
    id          BIGSERIAL    PRIMARY KEY,
    order_id    BIGINT       NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    event_type  VARCHAR(60)  NOT NULL,
    description VARCHAR(500),
    payload     JSONB,
    actor       VARCHAR(120),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_order_timeline_order_id   ON order_timeline(order_id);
CREATE INDEX idx_order_timeline_event_type ON order_timeline(event_type);

-- Índices de busca administrativa
CREATE INDEX idx_orders_created_at         ON orders(created_at DESC);
CREATE INDEX idx_orders_confirmed_at       ON orders(confirmed_at);
CREATE INDEX idx_orders_cancelled_at       ON orders(cancelled_at);
CREATE INDEX idx_order_items_sku_code      ON order_items(sku_code);
CREATE INDEX idx_customers_name_trgm       ON customers USING gin (lower(name) gin_trgm_ops);
CREATE INDEX idx_customers_phone           ON customers(phone);
