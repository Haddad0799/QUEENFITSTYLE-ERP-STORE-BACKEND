-- Produto publicado no catálogo (snapshot desnormalizado para leitura do storefront)
CREATE TABLE catalog_products (
    id              BIGSERIAL       PRIMARY KEY,
    product_id      BIGINT          NOT NULL UNIQUE,
    name            VARCHAR(255)    NOT NULL,
    description     TEXT,
    slug            VARCHAR(255)    NOT NULL UNIQUE,
    category_name   VARCHAR(100)    NOT NULL,
    main_image_url  VARCHAR(500),
    min_price       NUMERIC(10,2),
    published_at    TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- SKU ativo no catálogo
CREATE TABLE catalog_skus (
    id                  BIGSERIAL       PRIMARY KEY,
    catalog_product_id  BIGINT          NOT NULL REFERENCES catalog_products(id) ON DELETE CASCADE,
    sku_id              BIGINT          NOT NULL UNIQUE,
    code                VARCHAR(100)    NOT NULL,
    color_name          VARCHAR(50)     NOT NULL,
    color_hex           CHAR(7)         NOT NULL,
    size_name           VARCHAR(20)     NOT NULL,
    selling_price       NUMERIC(10,2)   NOT NULL,
    available_stock     INTEGER         NOT NULL DEFAULT 0,
    width               NUMERIC(10,2),
    height              NUMERIC(10,2),
    length              NUMERIC(10,2),
    weight              NUMERIC(10,2),
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Imagens do SKU no catálogo (URLs públicas já resolvidas)
CREATE TABLE catalog_sku_images (
    id              BIGSERIAL       PRIMARY KEY,
    catalog_sku_id  BIGINT          NOT NULL REFERENCES catalog_skus(id) ON DELETE CASCADE,
    image_url       VARCHAR(500)    NOT NULL,
    "order"         INT             NOT NULL DEFAULT 1
);

-- Índices para filtros e busca
CREATE INDEX idx_catalog_products_slug      ON catalog_products(slug);
CREATE INDEX idx_catalog_products_category  ON catalog_products(category_name);
CREATE INDEX idx_catalog_products_min_price ON catalog_products(min_price);
CREATE INDEX idx_catalog_skus_product       ON catalog_skus(catalog_product_id);
CREATE INDEX idx_catalog_skus_code          ON catalog_skus(code);
CREATE INDEX idx_catalog_skus_color         ON catalog_skus(color_name);
CREATE INDEX idx_catalog_skus_size          ON catalog_skus(size_name);
CREATE INDEX idx_catalog_skus_price         ON catalog_skus(selling_price);
CREATE INDEX idx_catalog_skus_stock         ON catalog_skus(available_stock);

