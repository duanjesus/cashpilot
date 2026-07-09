CREATE TABLE parcelamentos (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    categoria_id BIGINT NOT NULL REFERENCES categorias(id),
    conta_bancaria_id BIGINT NULL REFERENCES contas_bancarias(id),
    cartao_credito_id BIGINT NULL REFERENCES cartoes_credito(id),
    descricao VARCHAR(200) NOT NULL,
    valor_total NUMERIC(14,2) NOT NULL CHECK (valor_total > 0),
    numero_parcelas INTEGER NOT NULL CHECK (numero_parcelas >= 2),
    data_primeira_parcela DATE NOT NULL,
    observacoes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_parcelamentos_forma_pagamento CHECK (conta_bancaria_id IS NOT NULL OR cartao_credito_id IS NOT NULL)
);

CREATE INDEX idx_parcelamentos_user_id ON parcelamentos(user_id);
