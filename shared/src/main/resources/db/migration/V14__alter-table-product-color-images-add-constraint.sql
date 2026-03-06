ALTER TABLE product_color_images
    ADD CONSTRAINT uq_product_color_image_key
        UNIQUE (product_id, color_id, image_key);