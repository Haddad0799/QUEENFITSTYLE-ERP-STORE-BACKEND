ALTER TABLE products
    ADD COLUMN primary_color_id BIGINT REFERENCES colors(id);