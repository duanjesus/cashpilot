ALTER TABLE despesas
    ADD COLUMN assinatura_id BIGINT NULL REFERENCES assinaturas(id) ON DELETE SET NULL,
    ADD COLUMN referencia_mes DATE NULL;

CREATE UNIQUE INDEX uq_despesas_assinatura_referencia_mes
    ON despesas(assinatura_id, referencia_mes)
    WHERE assinatura_id IS NOT NULL;
