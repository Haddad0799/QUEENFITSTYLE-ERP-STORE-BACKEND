CREATE INDEX idx_catalog_skus_sellable_product
    ON catalog_skus(catalog_product_id)
    WHERE available_stock > 0;
