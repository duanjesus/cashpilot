CREATE TABLE despesas (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    categoria_id BIGINT NOT NULL REFERENCES categorias(id),
    conta_bancaria_id BIGINT NULL REFERENCES contas_bancarias(id),
    cartao_credito_id BIGINT NULL REFERENCES cartoes_credito(id),
    descricao VARCHAR(200) NOT NULL,
    valor NUMERIC(14,2) NOT NULL CHECK (valor > 0),
    data DATE NOT NULL,
    paga BOOLEAN NOT NULL DEFAULT TRUE,
    data_pagamento DATE,
    observacoes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_despesas_forma_pagamento CHECK (conta_bancaria_id IS NOT NULL OR cartao_credito_id IS NOT NULL)
);

CREATE INDEX idx_despesas_user_id_data ON despesas(user_id, data);
CREATE INDEX idx_despesas_cartao_id_paga ON despesas(cartao_credito_id, paga);
