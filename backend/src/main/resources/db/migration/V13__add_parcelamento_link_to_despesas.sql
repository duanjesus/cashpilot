ALTER TABLE despesas
    ADD COLUMN parcelamento_id BIGINT NULL REFERENCES parcelamentos(id),
    ADD COLUMN numero_parcela INTEGER NULL;

CREATE INDEX idx_despesas_parcelamento_id ON despesas(parcelamento_id);
