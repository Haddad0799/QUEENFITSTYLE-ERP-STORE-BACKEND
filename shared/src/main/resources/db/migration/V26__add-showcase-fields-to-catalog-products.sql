ALTER TABLE catalog_products
    ADD COLUMN main_color_name VARCHAR(50),
    ADD COLUMN main_color_hex CHAR(7),
    ADD COLUMN default_sku_code VARCHAR(100),
    ADD COLUMN default_selection_label VARCHAR(20),
    ADD COLUMN display_price NUMERIC(10,2);

UPDATE catalog_products cp
SET main_color_name = (
        SELECT ccg.color_name
        FROM catalog_color_groups ccg
        JOIN catalog_color_images cci ON cci.catalog_color_group_id = ccg.id
        WHERE ccg.catalog_product_id = cp.id
          AND cci.image_url = cp.main_image_url
        ORDER BY cci."order", ccg.id
        LIMIT 1
    ),
    main_color_hex = (
        SELECT ccg.color_hex
        FROM catalog_color_groups ccg
        JOIN catalog_color_images cci ON cci.catalog_color_group_id = ccg.id
        WHERE ccg.catalog_product_id = cp.id
          AND cci.image_url = cp.main_image_url
        ORDER BY cci."order", ccg.id
        LIMIT 1
    );

UPDATE catalog_products cp
SET (default_sku_code, default_selection_label, display_price) = (
    SELECT cs.code, cs.size_name, cs.selling_price
    FROM catalog_skus cs
    JOIN catalog_color_groups ccg ON ccg.id = cs.catalog_color_group_id
    WHERE cs.catalog_product_id = cp.id
      AND cs.available_stock > 0
      AND cs.selling_price IS NOT NULL
      AND (cp.main_color_name IS NULL OR ccg.color_name = cp.main_color_name)
    ORDER BY cs.selling_price, cs.size_name, cs.code
    LIMIT 1
);

UPDATE catalog_products cp
SET (main_color_name, main_color_hex, default_sku_code, default_selection_label, display_price) = (
    SELECT ccg.color_name, ccg.color_hex, cs.code, cs.size_name, cs.selling_price
    FROM catalog_skus cs
    JOIN catalog_color_groups ccg ON ccg.id = cs.catalog_color_group_id
    WHERE cs.catalog_product_id = cp.id
      AND cs.available_stock > 0
      AND cs.selling_price IS NOT NULL
    ORDER BY cs.selling_price, cs.size_name, cs.code
    LIMIT 1
)
WHERE cp.main_color_name IS NULL
  AND cp.default_sku_code IS NULL;

CREATE INDEX idx_catalog_products_display_price ON catalog_products(display_price);
