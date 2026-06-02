-- Adiciona suporte ao novo status PENDING_PAYMENT na coluna status de orders.
--
-- A coluna orders.status é VARCHAR(50) livre, sem CHECK CONSTRAINT nem tipo ENUM no banco
-- (ver V32__create_orders_tables.sql). Portanto o novo valor de enum no Java não exige
-- nenhuma alteração de tipo/constraint na coluna e nenhuma linha existente precisa migrar.
--
-- A única alteração necessária é no índice parcial de expiração: ele estava restrito a
-- WAITING_SELLER_CONFIRMATION, mas pedidos PENDING_PAYMENT também expiram pelo job agendado
-- (findExpiredPending). Recriamos o índice para cobrir os dois estados pendentes.

DROP INDEX IF EXISTS idx_orders_expires_pending;

CREATE INDEX idx_orders_expires_pending ON orders(expires_at)
    WHERE status IN ('WAITING_SELLER_CONFIRMATION', 'PENDING_PAYMENT');
