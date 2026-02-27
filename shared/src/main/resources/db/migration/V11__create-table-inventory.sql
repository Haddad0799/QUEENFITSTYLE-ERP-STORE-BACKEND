
CREATE TABLE sku_stock (
                           id              BIGSERIAL   PRIMARY KEY,
                           sku_id          BIGINT      NOT NULL UNIQUE,
                           quantity        INTEGER     NOT NULL DEFAULT 0,
                           reserved        INTEGER     NOT NULL DEFAULT 0,
                           min_quantity    INTEGER     NOT NULL DEFAULT 0,
                           created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
                           updated_at      TIMESTAMP   NOT NULL DEFAULT NOW(),

                           CONSTRAINT chk_quantity     CHECK (quantity >= 0),
                           CONSTRAINT chk_reserved     CHECK (reserved >= 0),
                           CONSTRAINT chk_min_quantity CHECK (min_quantity >= 0),
                           CONSTRAINT chk_available    CHECK (quantity >= reserved)
);

CREATE TABLE stock_movement (
                                id              BIGSERIAL   PRIMARY KEY,
                                sku_id          BIGINT      NOT NULL,
                                type            VARCHAR(20) NOT NULL,
                                quantity        INTEGER     NOT NULL,
                                reason          VARCHAR(100),
                                reference_id    BIGINT,
                                created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),

                                CONSTRAINT chk_movement_quantity CHECK (
                                    (type = 'INITIALIZED' AND quantity = 0) OR quantity > 0
                                    ),
                                CONSTRAINT fk_movement_sku FOREIGN KEY (sku_id) REFERENCES sku_stock(sku_id)
);

CREATE INDEX idx_stock_movement_sku_id ON stock_movement(sku_id);
CREATE INDEX idx_stock_movement_type   ON stock_movement(type);