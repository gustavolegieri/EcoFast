-- Estrutura do banco EcoFast (H2). A versão MySQL está em ../banco/ecofast.sql

CREATE TABLE ponto_coleta (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome     VARCHAR(100) NOT NULL,
    endereco VARCHAR(200) NOT NULL
);

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
);

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
);
