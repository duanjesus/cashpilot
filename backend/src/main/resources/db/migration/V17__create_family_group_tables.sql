CREATE TABLE family_groups (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    owner_user_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE family_group_members (
    id BIGSERIAL PRIMARY KEY,
    family_group_id BIGINT NOT NULL REFERENCES family_groups(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    role VARCHAR(20) NOT NULL,
    joined_at TIMESTAMP NOT NULL DEFAULT now(),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE family_invites (
    id BIGSERIAL PRIMARY KEY,
    family_group_id BIGINT NOT NULL REFERENCES family_groups(id) ON DELETE CASCADE,
    invited_email VARCHAR(150) NOT NULL,
    invited_role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    responded_at TIMESTAMP
);

CREATE INDEX idx_family_invites_email_status ON family_invites (invited_email, status);
