CREATE TABLE projecao_preferencias (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    salario NUMERIC(14,2),
    despesas_fixas NUMERIC(14,2),
    despesas_variaveis NUMERIC(14,2),
    investimento_mensal NUMERIC(14,2),
    valor_alvo NUMERIC(14,2),
    patrimonio_atual NUMERIC(14,2),
    taxa_retorno_mensal NUMERIC(6,4),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
