CREATE TABLE notificacoes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    tipo VARCHAR(30) NOT NULL,
    mensagem VARCHAR(500) NOT NULL,
    referencia_tipo VARCHAR(30) NOT NULL,
    referencia_id BIGINT NOT NULL,
    ano_mes_referencia VARCHAR(7),
    lida BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uk_notificacoes_dedupe ON notificacoes (user_id, tipo, referencia_tipo, referencia_id, ano_mes_referencia);
