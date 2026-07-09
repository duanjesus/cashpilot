ALTER TABLE receitas
    ADD COLUMN recebida BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN data_recebimento DATE;

CREATE INDEX idx_receitas_user_id_recebida ON receitas(user_id, recebida);
