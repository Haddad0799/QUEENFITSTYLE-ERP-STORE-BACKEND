-- Peso (weight) removido do cadastro de SKU: entregas são feitas pessoalmente
-- pela lojista, então o peso não tem utilidade operacional.
ALTER TABLE skus         DROP COLUMN IF EXISTS weight;
ALTER TABLE catalog_skus DROP COLUMN IF EXISTS weight;
