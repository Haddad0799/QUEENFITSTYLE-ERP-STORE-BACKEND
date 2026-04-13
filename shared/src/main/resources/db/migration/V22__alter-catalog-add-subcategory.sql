-- Renomeia as colunas existentes de category para representar a subcategoria (que é a categoria direta do produto)
-- e adiciona colunas para a categoria pai

-- Subcategoria (já existe como category, renomeia para semântica correta)
ALTER TABLE catalog_products
    ADD COLUMN subcategory_id BIGINT,
    ADD COLUMN subcategory_name VARCHAR(100),
    ADD COLUMN subcategory_normalized_name VARCHAR(100);

-- Categoria pai
ALTER TABLE catalog_products
    ADD COLUMN parent_category_id BIGINT,
    ADD COLUMN parent_category_name VARCHAR(100),
    ADD COLUMN parent_category_normalized_name VARCHAR(100);

-- Preenche subcategory a partir dos dados existentes (category_name = subcategoria)
UPDATE catalog_products cp
SET subcategory_id = c.id,
    subcategory_name = c.display_name,
    subcategory_normalized_name = c.normalized_name,
    parent_category_id = pc.id,
    parent_category_name = pc.display_name,
    parent_category_normalized_name = pc.normalized_name
FROM products p
         JOIN categories c ON c.id = p.category_id
         LEFT JOIN categories pc ON pc.id = c.parent_id
WHERE cp.product_id = p.id;

-- Índices para filtros
CREATE INDEX idx_catalog_products_subcategory_normalized ON catalog_products(subcategory_normalized_name);
CREATE INDEX idx_catalog_products_parent_category_normalized ON catalog_products(parent_category_normalized_name);

