ALTER TABLE skus
    ADD CONSTRAINT uk_product_color_size
        UNIQUE (product_id, color_id, size_id);
