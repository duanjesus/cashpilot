CREATE TABLE saldos_diarios (
    id BIGSERIAL PRIMARY KEY,
    conta_bancaria_id BIGINT NOT NULL REFERENCES contas_bancarias(id) ON DELETE CASCADE,
    data DATE NOT NULL,
    saldo NUMERIC(14,2) NOT NULL,
    origem VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uk_saldos_diarios_conta_data UNIQUE (conta_bancaria_id, data)
);
