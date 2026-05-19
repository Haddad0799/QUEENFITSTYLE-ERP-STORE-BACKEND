-- Add typed UUID reference column to stock_movement to store reservation/order UUIDs
ALTER TABLE stock_movement
    ADD COLUMN IF NOT EXISTS reference_uuid UUID NULL;

-- JSON metadata for future extensibility
ALTER TABLE stock_movement
    ADD COLUMN IF NOT EXISTS metadata JSONB NULL;

CREATE INDEX IF NOT EXISTS idx_stock_movement_reference_uuid ON stock_movement(reference_uuid);

