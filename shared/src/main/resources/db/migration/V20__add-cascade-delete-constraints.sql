-- V20__add-cascade-delete-constraints.sql
-- Adiciona ON DELETE CASCADE nas FKs necessárias para exclusão física de produtos e SKUs.
-- Futuramente, quando houver vendas, a exclusão será lógica (soft-delete com deleted_at).

-- 1. products.primary_image_id → product_color_images(id): trocar para ON DELETE SET NULL
--    Assim, ao deletar imagens, o campo primary_image_id é limpo automaticamente.
ALTER TABLE products DROP CONSTRAINT IF EXISTS products_primary_image_id_fkey;
ALTER TABLE products
    ADD CONSTRAINT products_primary_image_id_fkey
        FOREIGN KEY (primary_image_id) REFERENCES product_color_images(id) ON DELETE SET NULL;

-- 2. product_color_images.product_id → products(id): adicionar CASCADE
ALTER TABLE product_color_images DROP CONSTRAINT IF EXISTS product_color_images_product_id_fkey;
ALTER TABLE product_color_images
    ADD CONSTRAINT product_color_images_product_id_fkey
        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE;

-- 3. skus.product_id → products(id): adicionar FK com CASCADE (não existia FK formal)
ALTER TABLE skus
    ADD CONSTRAINT skus_product_id_fkey
        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE;

-- 4. sku_price.sku_id → skus(id): trocar para CASCADE
ALTER TABLE sku_price DROP CONSTRAINT IF EXISTS sku_price_sku_id_fkey;
ALTER TABLE sku_price
    ADD CONSTRAINT sku_price_sku_id_fkey
        FOREIGN KEY (sku_id) REFERENCES skus(id) ON DELETE CASCADE;

-- 5. sku_stock.sku_id: adicionar FK para skus(id) com CASCADE
ALTER TABLE sku_stock
    ADD CONSTRAINT sku_stock_sku_id_fkey
        FOREIGN KEY (sku_id) REFERENCES skus(id) ON DELETE CASCADE;

-- 6. stock_movement.fk_movement_sku → sku_stock(sku_id): trocar para CASCADE
ALTER TABLE stock_movement DROP CONSTRAINT IF EXISTS fk_movement_sku;
ALTER TABLE stock_movement
    ADD CONSTRAINT fk_movement_sku
        FOREIGN KEY (sku_id) REFERENCES sku_stock(sku_id) ON DELETE CASCADE;

