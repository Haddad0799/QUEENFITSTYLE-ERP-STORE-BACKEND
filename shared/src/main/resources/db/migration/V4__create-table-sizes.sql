CREATE TABLE sizes (
                       id BIGSERIAL PRIMARY KEY,
                       label VARCHAR(10) NOT NULL,
                       type VARCHAR(10) NOT NULL,
                       display_order INT NOT NULL,
                       UNIQUE (label, type)
);
