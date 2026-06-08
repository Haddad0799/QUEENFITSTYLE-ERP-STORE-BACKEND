-- Consolida o fluxo de pedidos no modelo de pagamento manual:
--   PENDING_PAYMENT -> PAID -> DELIVERED, com CANCELLED / EXPIRED / RETURNED.
--
-- Os status antigos (WAITING_SELLER_CONFIRMATION, CONFIRMED, PREPARING, SHIPPED) deixam de
-- existir no domínio. A coluna orders.status é VARCHAR livre (sem CHECK/ENUM no banco),
-- então a migração apenas converte as linhas existentes e ajusta o default e o índice parcial.

-- 1. Converte pedidos pendentes antigos para o novo estado inicial
UPDATE orders SET status = 'PENDING_PAYMENT' WHERE status = 'WAITING_SELLER_CONFIRMATION';

-- 2. Pedidos já confirmados/em preparo/enviados passam a ser PAID (reserva já consumida)
UPDATE orders SET status = 'PAID' WHERE status IN ('CONFIRMED', 'PREPARING', 'SHIPPED');

-- 3. Novo default da coluna: todo pedido nasce aguardando pagamento
ALTER TABLE orders ALTER COLUMN status SET DEFAULT 'PENDING_PAYMENT';

-- 4. Índice parcial de expiração: agora só pedidos PENDING_PAYMENT expiram pelo job agendado
DROP INDEX IF EXISTS idx_orders_expires_pending;
CREATE INDEX idx_orders_expires_pending ON orders(expires_at)
    WHERE status = 'PENDING_PAYMENT';
