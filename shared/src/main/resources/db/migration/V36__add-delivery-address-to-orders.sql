ALTER TABLE orders
    ADD COLUMN delivery_cep          VARCHAR(9),
    ADD COLUMN delivery_street       VARCHAR(255),
    ADD COLUMN delivery_number       VARCHAR(20),
    ADD COLUMN delivery_complement   VARCHAR(100),
    ADD COLUMN delivery_neighborhood VARCHAR(100),
    ADD COLUMN delivery_city         VARCHAR(100) DEFAULT 'Pirenópolis',
    ADD COLUMN delivery_state        VARCHAR(2)   DEFAULT 'GO';
