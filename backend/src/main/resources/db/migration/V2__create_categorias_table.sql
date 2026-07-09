CREATE TABLE categorias (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NULL REFERENCES users(id),
    nome VARCHAR(100) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    is_investment BOOLEAN NOT NULL DEFAULT FALSE,
    cor VARCHAR(7),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_categorias_user_id ON categorias(user_id);
