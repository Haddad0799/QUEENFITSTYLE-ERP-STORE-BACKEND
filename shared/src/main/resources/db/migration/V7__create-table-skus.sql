CREATE TABLE skus (
                      id BIGSERIAL PRIMARY KEY,
                      product_id BIGINT NOT NULL,
                      sku_code VARCHAR(100) NOT NULL,
                      color_id BIGINT NOT NULL,
                      size_id BIGINT NOT NULL,
                      width NUMERIC,
                      height NUMERIC,
                      length NUMERIC,
                      weight NUMERIC,
                      active BOOLEAN NOT NULL,
                      UNIQUE(product_id, color_id, size_id)
);
