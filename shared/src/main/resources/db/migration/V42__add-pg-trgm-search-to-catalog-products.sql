-- Busca tolerante a erros de digitação na vitrine (GET /store/products?search=)
-- Habilita a extensão pg_trgm e cria índices GIN de trigrama nos campos buscáveis
-- da projeção do catálogo: nome do produto, nome da subcategoria e da categoria pai.

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_catalog_products_name_trgm
    ON catalog_products USING gin (name gin_trgm_ops);

CREATE INDEX idx_catalog_products_subcategory_name_trgm
    ON catalog_products USING gin (subcategory_name gin_trgm_ops);

CREATE INDEX idx_catalog_products_parent_category_name_trgm
    ON catalog_products USING gin (parent_category_name gin_trgm_ops);
