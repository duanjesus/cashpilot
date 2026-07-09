CREATE TABLE transferencias (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    conta_origem_id BIGINT NOT NULL REFERENCES contas_bancarias(id),
    conta_destino_id BIGINT NOT NULL REFERENCES contas_bancarias(id),
    valor NUMERIC(14,2) NOT NULL CHECK (valor > 0),
    data DATE NOT NULL,
    descricao VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_transferencias_contas_distintas CHECK (conta_origem_id <> conta_destino_id)
);

CREATE INDEX idx_transferencias_user_id_data ON transferencias(user_id, data);
