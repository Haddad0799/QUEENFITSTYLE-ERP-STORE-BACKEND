CREATE TABLE products (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          description TEXT,
                          slug VARCHAR(255) NOT NULL UNIQUE,
                          category_id BIGINT NOT NULL,
                          active BOOLEAN NOT NULL DEFAULT FALSE
);
