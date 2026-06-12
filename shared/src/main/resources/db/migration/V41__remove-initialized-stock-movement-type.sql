-- O tipo INITIALIZED não tem mais uso: a criação de estoque com quantidade 0
-- não gera movimentação. Remove as linhas remanescentes e aperta a constraint
-- para exigir sempre quantity > 0.
DELETE FROM stock_movement WHERE type = 'INITIALIZED';

ALTER TABLE stock_movement DROP CONSTRAINT IF EXISTS chk_movement_quantity;
ALTER TABLE stock_movement ADD CONSTRAINT chk_movement_quantity CHECK (quantity > 0);
