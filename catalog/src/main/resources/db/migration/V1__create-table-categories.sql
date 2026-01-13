CREATE TABLE categories (
                            id BIGSERIAL PRIMARY KEY,
                            display_name VARCHAR(255) NOT NULL,
                            normalized_name VARCHAR(255) NOT NULL UNIQUE,
                            active BOOLEAN NOT NULL DEFAULT true
);
