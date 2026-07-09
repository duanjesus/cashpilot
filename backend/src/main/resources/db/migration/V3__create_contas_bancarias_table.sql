CREATE TABLE contas_bancarias (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    nome VARCHAR(100) NOT NULL,
    instituicao VARCHAR(100),
    tipo VARCHAR(20) NOT NULL,
    saldo_inicial NUMERIC(14,2) NOT NULL DEFAULT 0,
    data_saldo_inicial DATE NOT NULL,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_contas_bancarias_user_id ON contas_bancarias(user_id);
