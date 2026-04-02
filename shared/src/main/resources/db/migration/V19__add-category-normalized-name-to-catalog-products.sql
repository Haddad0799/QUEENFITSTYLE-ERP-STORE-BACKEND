ALTER TABLE catalog_products
    ADD COLUMN category_normalized_name VARCHAR(100);

-- Preenche com o valor normalizado das categorias existentes
UPDATE catalog_products cp
SET category_normalized_name = c.normalized_name
FROM products p
         JOIN categories c ON c.id = p.category_id
WHERE cp.product_id = p.id;

-- Depois de preencher, torna NOT NULL
ALTER TABLE catalog_products
    ALTER COLUMN category_normalized_name SET NOT NULL;

-- Índice para filtro por nome normalizado
CREATE INDEX idx_catalog_products_category_normalized ON catalog_products(category_normalized_name);

