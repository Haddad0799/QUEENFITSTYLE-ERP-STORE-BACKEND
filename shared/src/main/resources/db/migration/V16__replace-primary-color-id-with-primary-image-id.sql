-- Remove a FK antiga baseada em cor
ALTER TABLE products DROP COLUMN primary_color_id;

-- Adiciona a nova coluna apontando diretamente para a imagem
ALTER TABLE products ADD COLUMN primary_image_id BIGINT REFERENCES product_color_images(id);

