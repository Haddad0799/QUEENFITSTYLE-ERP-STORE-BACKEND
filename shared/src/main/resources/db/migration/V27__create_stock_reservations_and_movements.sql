-- Create only the new objects required by inventory: reservations table.
-- NOTE: the project already has a `sku_stock` and `stock_movement` tables (see V11__create-table-inventory.sql).
-- This migration must not reference a non-existing table `sku` nor duplicate existing tables with incompatible schemas.

CREATE TABLE IF NOT EXISTS stock_reservations (
  reservation_id UUID PRIMARY KEY,
  sku_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_by VARCHAR(100),
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  expires_at TIMESTAMP WITHOUT TIME ZONE,
  metadata JSONB NULL,
  -- reference the existing sku_stock by its sku_id column
  CONSTRAINT fk_reservation_sku FOREIGN KEY (sku_id) REFERENCES sku_stock(sku_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_stock_reservations_sku_status ON stock_reservations(sku_id, status);
CREATE INDEX IF NOT EXISTS idx_stock_reservations_expires_at ON stock_reservations(expires_at);

-- We do NOT create `stock_movement` here because it already exists (created by earlier migration V11).
-- If you intentionally want to migrate to a different movements table (e.g. with UUID reference),
-- add a dedicated migration that performs a careful ALTER TABLE and data migration.

