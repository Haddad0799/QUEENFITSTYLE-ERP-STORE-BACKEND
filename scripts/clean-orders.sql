-- ============================================================================
-- clean-orders.sql — limpa pedidos (e, opcionalmente, as reservas de estoque)
-- ----------------------------------------------------------------------------
-- Uso (dev/staging):
--   psql -h localhost -p 5432 -U $POSTGRES_USER -d $POSTGRES_DB -f scripts/clean-orders.sql
--   ou via docker:
--   docker exec -i <container_pg> psql -U $POSTGRES_USER -d $POSTGRES_DB < scripts/clean-orders.sql
--
-- ⚠️  DESTRUTIVO e IRREVERSÍVEL. Apaga TODOS os pedidos. NÃO rode em produção
--     sem backup. Roda dentro de uma transação: em caso de erro nada é aplicado.
--
-- Estrutura:
--   SEÇÃO 1 — Pedidos (sempre): orders + order_items + order_timeline
--   SEÇÃO 2 — Reservas de estoque (opcional, "caso precise"): stock_reservations
--             + recálculo de sku_stock.reserved (+ catalog_skus.available_stock)
-- ============================================================================

BEGIN;

-- ----------------------------------------------------------------------------
-- SEÇÃO 1 — Pedidos
-- ----------------------------------------------------------------------------
-- TRUNCATE ... CASCADE remove também order_items e order_timeline (FK ON DELETE
-- CASCADE) e RESTART IDENTITY zera os BIGSERIAL para os IDs recomeçarem em 1.
TRUNCATE TABLE orders RESTART IDENTITY CASCADE;

-- ----------------------------------------------------------------------------
-- SEÇÃO 2 — Reservas de estoque  (descomente este bloco SÓ se precisar resetar
--           o estoque reservado pelos pedidos que acabaram de ser apagados)
-- ----------------------------------------------------------------------------
-- Sem este bloco, sku_stock.reserved continua contabilizando reservas órfãs
-- (pedidos apagados mas reservas RESERVED nunca liberadas) → estoque disponível
-- aparece menor do que realmente é.
--
-- TRUNCATE TABLE stock_reservations;
--
-- -- Recalcula o reservado de cada SKU a partir das reservas que sobraram
-- -- (após o TRUNCATE acima, todas ficam em 0). Não mexe em `quantity`: o estoque
-- -- já consumido por reservas CONFIRMED permanece consumido.
-- UPDATE sku_stock s
--    SET reserved   = COALESCE((
--            SELECT SUM(r.quantity)
--              FROM stock_reservations r
--             WHERE r.sku_id = s.sku_id
--               AND r.status = 'RESERVED'
--        ), 0),
--        updated_at = NOW();
--
-- -- (Opcional) Ressincroniza o read model público do catálogo. Normalmente isso
-- -- é feito pela aplicação via eventos; só rode se for resetar tudo manualmente.
-- UPDATE catalog_skus c
--    SET available_stock = COALESCE((
--            SELECT s.quantity - s.reserved
--              FROM sku_stock s
--             WHERE s.sku_id = c.sku_id
--        ), c.available_stock);

-- ----------------------------------------------------------------------------
-- Confira o resultado antes de confirmar:
-- ----------------------------------------------------------------------------
-- SELECT
--   (SELECT COUNT(*) FROM orders)             AS orders,
--   (SELECT COUNT(*) FROM order_items)         AS order_items,
--   (SELECT COUNT(*) FROM order_timeline)      AS order_timeline,
--   (SELECT COUNT(*) FROM stock_reservations)  AS reservations;

COMMIT;
-- ROLLBACK;  -- troque COMMIT por ROLLBACK acima para um "dry-run" sem aplicar nada
