CREATE TABLE assinaturas (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    categoria_id BIGINT NOT NULL REFERENCES categorias(id),
    conta_bancaria_id BIGINT NULL REFERENCES contas_bancarias(id),
    cartao_credito_id BIGINT NULL REFERENCES cartoes_credito(id),
    descricao VARCHAR(200) NOT NULL,
    valor NUMERIC(14,2) NOT NULL CHECK (valor > 0),
    dia_cobranca INTEGER NOT NULL CHECK (dia_cobranca BETWEEN 1 AND 31),
    data_inicio DATE NOT NULL,
    data_fim DATE NULL,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    observacoes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_assinaturas_forma_pagamento CHECK (conta_bancaria_id IS NOT NULL OR cartao_credito_id IS NOT NULL),
    CONSTRAINT chk_assinaturas_datas CHECK (data_fim IS NULL OR data_fim >= data_inicio)
);

CREATE INDEX idx_assinaturas_user_id ON assinaturas(user_id);
CREATE INDEX idx_assinaturas_ativa ON assinaturas(ativa);
