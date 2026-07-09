CREATE TABLE metas_financeiras (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    nome VARCHAR(150) NOT NULL,
    valor_alvo NUMERIC(14,2) NOT NULL,
    data_alvo DATE NOT NULL,
    valor_atual NUMERIC(14,2) NOT NULL DEFAULT 0,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_metas_financeiras_user_id ON metas_financeiras(user_id);
