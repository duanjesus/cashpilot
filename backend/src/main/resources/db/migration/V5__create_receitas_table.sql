CREATE TABLE receitas (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    categoria_id BIGINT NOT NULL REFERENCES categorias(id),
    conta_bancaria_id BIGINT NOT NULL REFERENCES contas_bancarias(id),
    descricao VARCHAR(200) NOT NULL,
    valor NUMERIC(14,2) NOT NULL CHECK (valor > 0),
    data DATE NOT NULL,
    recorrente BOOLEAN NOT NULL DEFAULT FALSE,
    observacoes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_receitas_user_id_data ON receitas(user_id, data);
CREATE INDEX idx_receitas_categoria_id ON receitas(categoria_id);
