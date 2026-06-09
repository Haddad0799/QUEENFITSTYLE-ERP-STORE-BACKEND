-- Ambiente de testes apenas
-- Limpa os dados de pedidos e clientes para permitir a adição de
-- customers.email como NOT NULL (V39) numa tabela sem linhas órfãs.
-- DELETEs na ordem correta: filhos antes dos pais (timeline -> itens -> pedidos -> clientes).

DELETE FROM order_timeline;
DELETE FROM order_items;
DELETE FROM orders;
DELETE FROM customers;
