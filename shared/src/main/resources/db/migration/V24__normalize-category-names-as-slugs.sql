CREATE EXTENSION IF NOT EXISTS unaccent;

UPDATE categories
SET normalized_name = trim(both '-' FROM regexp_replace(
        regexp_replace(
                lower(unaccent(display_name)),
                '[0-9]+',
                '',
                'g'
        ),
        '[^a-z]+',
        '-',
        'g'
));

UPDATE catalog_products cp
SET category_normalized_name = c.normalized_name,
    subcategory_normalized_name = c.normalized_name,
    parent_category_normalized_name = pc.normalized_name
FROM products p
         JOIN categories c ON c.id = p.category_id
         LEFT JOIN categories pc ON pc.id = c.parent_id
WHERE cp.product_id = p.id;
