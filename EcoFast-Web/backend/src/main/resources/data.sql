-- Dados de exemplo. Senha de todos os usuários: 123456 (SHA-256 abaixo)

INSERT INTO ponto_coleta (nome, endereco) VALUES
    ('Ecoponto Centro', 'R. Bom Jesus de Pirapora, 100 - Centro, Jundiaí - SP'),
    ('Ecoponto Vila Arens', 'Av. União dos Ferroviários, 1500 - Vila Arens, Jundiaí - SP');

INSERT INTO usuario (nome, email, senha_hash, perfil, cpf, matricula, ponto_coleta_id) VALUES
    ('Maria Silva',     'maria@email.com',     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'CIDADAO',     '111.111.111-11', NULL,   NULL),
    ('João Souza',      'joao@email.com',      '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'CIDADAO',     '222.222.222-22', NULL,   NULL),
    ('Ana Lima',        'ana@email.com',       '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'CIDADAO',     '333.333.333-33', NULL,   NULL),
    ('Carlos Pereira',  'carlos@email.com',    '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'CIDADAO',     '444.444.444-44', NULL,   NULL),
    ('Pedro Operador',  'funcionario@ecofast.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'FUNCIONARIO', NULL,         'F-1001', 1),
    ('Admin Ecoponto',  'admin@ecofast.com',   '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'ADMIN',       NULL,             'A-0001', 1);

-- Entregas dos últimos 30 dias (pontos = peso x multiplicador; eletrônico +20 de bônus)
INSERT INTO descarte (cidadao_id, funcionario_id, ponto_coleta_id, material, peso_kg, pontos, data_hora) VALUES
    (1, 5, 1, 'PLASTICO',    2.2,  22, DATEADD('HOUR', -2,   CURRENT_TIMESTAMP)),
    (2, 5, 1, 'METAL',       3.0,  45, DATEADD('HOUR', -4,   CURRENT_TIMESTAMP)),
    (1, 5, 1, 'PLASTICO',    4.5,  45, DATEADD('HOUR', -26,  CURRENT_TIMESTAMP)),
    (2, 5, 1, 'PAPEL',      20.4, 102, DATEADD('HOUR', -50,  CURRENT_TIMESTAMP)),
    (3, 5, 1, 'METAL',       5.0,  75, DATEADD('HOUR', -73,  CURRENT_TIMESTAMP)),
    (1, 5, 1, 'PAPEL',      12.0,  60, DATEADD('HOUR', -98,  CURRENT_TIMESTAMP)),
    (4, 6, 1, 'VIDRO',      15.5, 124, DATEADD('HOUR', -121, CURRENT_TIMESTAMP)),
    (2, 5, 1, 'PLASTICO',    7.8,  78, DATEADD('HOUR', -170, CURRENT_TIMESTAMP)),
    (4, 5, 1, 'PLASTICO',    9.1,  91, DATEADD('HOUR', -194, CURRENT_TIMESTAMP)),
    (1, 5, 1, 'METAL',       2.4,  36, DATEADD('HOUR', -217, CURRENT_TIMESTAMP)),
    (3, 5, 1, 'PLASTICO',    3.0,  30, DATEADD('HOUR', -266, CURRENT_TIMESTAMP)),
    (4, 5, 1, 'ELETRONICO',  4.0, 120, DATEADD('HOUR', -290, CURRENT_TIMESTAMP)),
    (3, 6, 1, 'ELETRONICO',  1.6,  60, DATEADD('HOUR', -313, CURRENT_TIMESTAMP)),
    (1, 5, 1, 'ELETRONICO',  3.2, 100, DATEADD('HOUR', -362, CURRENT_TIMESTAMP)),
    (3, 5, 1, 'VIDRO',       7.5,  60, DATEADD('HOUR', -386, CURRENT_TIMESTAMP)),
    (2, 5, 1, 'VIDRO',      10.0,  80, DATEADD('HOUR', -434, CURRENT_TIMESTAMP)),
    (4, 5, 1, 'PAPEL',      30.0, 150, DATEADD('HOUR', -482, CURRENT_TIMESTAMP)),
    (1, 5, 1, 'VIDRO',       6.0,  48, DATEADD('HOUR', -530, CURRENT_TIMESTAMP)),
    (2, 5, 1, 'PAPEL',      14.6,  73, DATEADD('HOUR', -578, CURRENT_TIMESTAMP)),
    (3, 5, 1, 'PAPEL',       8.2,  41, DATEADD('HOUR', -626, CURRENT_TIMESTAMP)),
    (4, 5, 1, 'METAL',       1.2,  18, DATEADD('HOUR', -650, CURRENT_TIMESTAMP)),
    (1, 5, 1, 'PAPEL',       9.4,  47, DATEADD('HOUR', -674, CURRENT_TIMESTAMP));

-- Saldo inicial = soma das entregas de cada cidadão
UPDATE usuario u SET saldo_ecocoins = (SELECT COALESCE(SUM(d.pontos), 0) FROM descarte d WHERE d.cidadao_id = u.id)
WHERE perfil = 'CIDADAO';
