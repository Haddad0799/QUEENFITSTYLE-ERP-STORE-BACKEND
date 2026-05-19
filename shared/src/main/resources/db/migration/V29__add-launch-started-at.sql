-- V24__add-launch-started-at.sql
ALTER TABLE products
    ADD COLUMN launch_started_at TIMESTAMPTZ;

-- Backfill for existing launches
UPDATE products
SET launch_started_at = now()
WHERE is_launch = TRUE AND launch_started_at IS NULL;

ALTER TABLE catalog_products
    ADD COLUMN launch_started_at TIMESTAMPTZ;

UPDATE catalog_products
SET launch_started_at = now()
WHERE is_launch = TRUE AND launch_started_at IS NULL;

CREATE INDEX idx_products_launch_started_at ON products(launch_started_at);
CREATE INDEX idx_catalog_products_launch_started_at ON catalog_products(launch_started_at);