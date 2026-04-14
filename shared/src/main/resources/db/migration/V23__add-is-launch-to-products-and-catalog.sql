ALTER TABLE products
    ADD COLUMN is_launch BOOLEAN;

UPDATE products
SET is_launch = FALSE
WHERE is_launch IS NULL;

ALTER TABLE products
    ALTER COLUMN is_launch SET DEFAULT FALSE;

ALTER TABLE products
    ALTER COLUMN is_launch SET NOT NULL;

ALTER TABLE catalog_products
    ADD COLUMN is_launch BOOLEAN;

UPDATE catalog_products
SET is_launch = FALSE
WHERE is_launch IS NULL;

ALTER TABLE catalog_products
    ALTER COLUMN is_launch SET DEFAULT FALSE;

ALTER TABLE catalog_products
    ALTER COLUMN is_launch SET NOT NULL;

CREATE INDEX idx_catalog_products_is_launch ON catalog_products(is_launch);
