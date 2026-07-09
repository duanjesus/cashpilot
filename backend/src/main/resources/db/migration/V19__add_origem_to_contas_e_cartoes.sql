ALTER TABLE contas_bancarias
    ADD COLUMN origem VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    ADD COLUMN instituicao_nome VARCHAR(150),
    ADD COLUMN ultima_sincronizacao TIMESTAMP;

ALTER TABLE cartoes_credito
    ADD COLUMN origem VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    ADD COLUMN instituicao_nome VARCHAR(150),
    ADD COLUMN ultima_sincronizacao TIMESTAMP;
