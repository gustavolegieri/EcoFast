-- EcoFast - script para MySQL/MariaDB (XAMPP / phpMyAdmin)
-- Importe este arquivo e rode o back-end com o perfil "mysql".
CREATE DATABASE IF NOT EXISTS ecofast CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE ecofast;

DROP TABLE IF EXISTS descarte;
DROP TABLE IF EXISTS usuario;
DROP TABLE IF EXISTS ponto_coleta;


CREATE TABLE ponto_coleta (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome     VARCHAR(100) NOT NULL,
    endereco VARCHAR(200) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Uma tabela para todos os perfis; a coluna "perfil" diz qual classe Java instanciar
CREATE TABLE usuario (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome            VARCHAR(100) NOT NULL,
    email           VARCHAR(120) NOT NULL UNIQUE,
    senha_hash      CHAR(64)     NOT NULL,
    perfil          VARCHAR(20)  NOT NULL,          -- CIDADAO | FUNCIONARIO | ADMIN
    cpf             VARCHAR(14)  UNIQUE,            -- só cidadão
    saldo_ecocoins  INT          NOT NULL DEFAULT 0,-- só cidadão
    matricula       VARCHAR(20),                    -- funcionário / admin
    ponto_coleta_id BIGINT,
    CONSTRAINT fk_usuario_ponto FOREIGN KEY (ponto_coleta_id) REFERENCES ponto_coleta (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE descarte (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    cidadao_id      BIGINT        NOT NULL,
    funcionario_id  BIGINT        NOT NULL,
    ponto_coleta_id BIGINT        NOT NULL,
    material        VARCHAR(20)   NOT NULL,         -- PAPEL | PLASTICO | VIDRO | METAL | ELETRONICO
    peso_kg         DECIMAL(7, 2) NOT NULL,
    pontos          INT           NOT NULL,
    data_hora       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_descarte_cidadao     FOREIGN KEY (cidadao_id)      REFERENCES usuario (id),
    CONSTRAINT fk_descarte_funcionario FOREIGN KEY (funcionario_id)  REFERENCES usuario (id),
    CONSTRAINT fk_descarte_ponto       FOREIGN KEY (ponto_coleta_id) REFERENCES ponto_coleta (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
    (1, 5, 1, 'PLASTICO',    2.2,  22, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
    (2, 5, 1, 'METAL',       3.0,  45, DATE_SUB(NOW(), INTERVAL 4 HOUR)),
    (1, 5, 1, 'PLASTICO',    4.5,  45, DATE_SUB(NOW(), INTERVAL 26 HOUR)),
    (2, 5, 1, 'PAPEL',      20.4, 102, DATE_SUB(NOW(), INTERVAL 50 HOUR)),
    (3, 5, 1, 'METAL',       5.0,  75, DATE_SUB(NOW(), INTERVAL 73 HOUR)),
    (1, 5, 1, 'PAPEL',      12.0,  60, DATE_SUB(NOW(), INTERVAL 98 HOUR)),
    (4, 6, 1, 'VIDRO',      15.5, 124, DATE_SUB(NOW(), INTERVAL 121 HOUR)),
    (2, 5, 1, 'PLASTICO',    7.8,  78, DATE_SUB(NOW(), INTERVAL 170 HOUR)),
    (4, 5, 1, 'PLASTICO',    9.1,  91, DATE_SUB(NOW(), INTERVAL 194 HOUR)),
    (1, 5, 1, 'METAL',       2.4,  36, DATE_SUB(NOW(), INTERVAL 217 HOUR)),
    (3, 5, 1, 'PLASTICO',    3.0,  30, DATE_SUB(NOW(), INTERVAL 266 HOUR)),
    (4, 5, 1, 'ELETRONICO',  4.0, 120, DATE_SUB(NOW(), INTERVAL 290 HOUR)),
    (3, 6, 1, 'ELETRONICO',  1.6,  60, DATE_SUB(NOW(), INTERVAL 313 HOUR)),
    (1, 5, 1, 'ELETRONICO',  3.2, 100, DATE_SUB(NOW(), INTERVAL 362 HOUR)),
    (3, 5, 1, 'VIDRO',       7.5,  60, DATE_SUB(NOW(), INTERVAL 386 HOUR)),
    (2, 5, 1, 'VIDRO',      10.0,  80, DATE_SUB(NOW(), INTERVAL 434 HOUR)),
    (4, 5, 1, 'PAPEL',      30.0, 150, DATE_SUB(NOW(), INTERVAL 482 HOUR)),
    (1, 5, 1, 'VIDRO',       6.0,  48, DATE_SUB(NOW(), INTERVAL 530 HOUR)),
    (2, 5, 1, 'PAPEL',      14.6,  73, DATE_SUB(NOW(), INTERVAL 578 HOUR)),
    (3, 5, 1, 'PAPEL',       8.2,  41, DATE_SUB(NOW(), INTERVAL 626 HOUR)),
    (4, 5, 1, 'METAL',       1.2,  18, DATE_SUB(NOW(), INTERVAL 650 HOUR)),
    (1, 5, 1, 'PAPEL',       9.4,  47, DATE_SUB(NOW(), INTERVAL 674 HOUR));

-- Saldo inicial = soma das entregas de cada cidadão
UPDATE usuario u SET saldo_ecocoins = (SELECT COALESCE(SUM(d.pontos), 0) FROM descarte d WHERE d.cidadao_id = u.id)
WHERE perfil = 'CIDADAO';
