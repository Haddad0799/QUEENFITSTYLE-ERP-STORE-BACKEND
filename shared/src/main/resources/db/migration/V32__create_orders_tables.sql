CREATE TABLE orders (
    id               BIGSERIAL     PRIMARY KEY,
    customer_id      BIGINT        NOT NULL REFERENCES customers(id),
    status           VARCHAR(50)   NOT NULL DEFAULT 'WAITING_SELLER_CONFIRMATION',
    total_amount     NUMERIC(10,2) NOT NULL,
    notes            TEXT,
    whatsapp_message TEXT,
    expires_at       TIMESTAMP,
    confirmed_at     TIMESTAMP,
    cancelled_at     TIMESTAMP,
    created_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_status      ON orders(status);
CREATE INDEX idx_orders_expires_pending ON orders(expires_at)
    WHERE status = 'WAITING_SELLER_CONFIRMATION';

CREATE TABLE order_items (
    id             BIGSERIAL     PRIMARY KEY,
    order_id       BIGINT        NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    sku_id         BIGINT        NOT NULL,
    reservation_id UUID          NOT NULL,
    product_name   VARCHAR(255)  NOT NULL,
    sku_code       VARCHAR(100)  NOT NULL,
    color_name     VARCHAR(100),
    size_label     VARCHAR(50),
    quantity       INT           NOT NULL CHECK (quantity > 0),
    unit_price     NUMERIC(10,2) NOT NULL,
    subtotal       NUMERIC(10,2) NOT NULL,
    created_at     TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_order_item_reservation UNIQUE (reservation_id)
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
