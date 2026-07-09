CREATE TABLE cartoes_credito (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    nome VARCHAR(100) NOT NULL,
    bandeira VARCHAR(20) NOT NULL,
    limite NUMERIC(14,2) NOT NULL,
    dia_fechamento INT NOT NULL CHECK (dia_fechamento BETWEEN 1 AND 31),
    dia_vencimento INT NOT NULL CHECK (dia_vencimento BETWEEN 1 AND 31),
    conta_vinculada_id BIGINT NULL REFERENCES contas_bancarias(id),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_cartoes_credito_user_id ON cartoes_credito(user_id);
