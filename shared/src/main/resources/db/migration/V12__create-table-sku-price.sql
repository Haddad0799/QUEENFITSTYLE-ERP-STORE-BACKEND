CREATE TABLE sku_price (
                           id            BIGSERIAL PRIMARY KEY,
                           sku_id        BIGINT       NOT NULL REFERENCES skus(id),
                           cost_price    NUMERIC(10,2) NOT NULL,
                           selling_price NUMERIC(10,2) NOT NULL,
                           created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);