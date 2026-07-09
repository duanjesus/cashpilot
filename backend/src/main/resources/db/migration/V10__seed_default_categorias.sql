INSERT INTO categorias (user_id, nome, tipo, is_system, is_investment, cor, created_at, updated_at) VALUES
(NULL, 'Salário',            'RECEITA', TRUE, FALSE, '#22c55e', now(), now()),
(NULL, 'Freelance',          'RECEITA', TRUE, FALSE, '#0ea5e9', now(), now()),
(NULL, 'Outras receitas',    'RECEITA', TRUE, FALSE, '#64748b', now(), now()),
(NULL, 'Alimentação',        'DESPESA', TRUE, FALSE, '#f97316', now(), now()),
(NULL, 'Moradia',            'DESPESA', TRUE, FALSE, '#a855f7', now(), now()),
(NULL, 'Transporte',         'DESPESA', TRUE, FALSE, '#3b82f6', now(), now()),
(NULL, 'Saúde',              'DESPESA', TRUE, FALSE, '#ef4444', now(), now()),
(NULL, 'Lazer',              'DESPESA', TRUE, FALSE, '#eab308', now(), now()),
(NULL, 'Educação',           'DESPESA', TRUE, FALSE, '#14b8a6', now(), now()),
(NULL, 'Outras despesas',    'DESPESA', TRUE, FALSE, '#94a3b8', now(), now()),
(NULL, 'Investimentos',      'AMBOS',   TRUE, TRUE,  '#059669', now(), now());
