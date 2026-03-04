CREATE TABLE product_color_images (
                                      id         BIGSERIAL PRIMARY KEY,
                                      product_id BIGINT       NOT NULL REFERENCES products(id),
                                      color_id   BIGINT       NOT NULL REFERENCES colors(id),
                                      image_key  VARCHAR(500) NOT NULL,
                                      "order"    INT          NOT NULL DEFAULT 1,
                                      created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);