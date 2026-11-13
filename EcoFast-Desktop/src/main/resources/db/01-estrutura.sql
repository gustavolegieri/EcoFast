-- EcoFast: estrutura do banco (SQL Server). Pode rodar quantas vezes quiser: só cria o que falta.
-- Os blocos são separados por GO, igual no SSMS.

IF OBJECT_ID(N'dbo.ponto_coleta', N'U') IS NULL
CREATE TABLE dbo.ponto_coleta (
    id        BIGINT IDENTITY(1,1) CONSTRAINT pk_ponto_coleta PRIMARY KEY,
    nome      NVARCHAR(100) NOT NULL,
    endereco  NVARCHAR(200) NOT NULL
);
GO

-- Uma tabela para todos os perfis; a coluna "perfil" diz qual classe Java instanciar
IF OBJECT_ID(N'dbo.usuario', N'U') IS NULL
CREATE TABLE dbo.usuario (
    id              BIGINT IDENTITY(1,1) CONSTRAINT pk_usuario PRIMARY KEY,
    nome            NVARCHAR(100) NOT NULL,
    email           NVARCHAR(120) NOT NULL CONSTRAINT uq_usuario_email UNIQUE,
    senha_hash      CHAR(64)      NOT NULL,
    perfil          VARCHAR(20)   NOT NULL
                    CONSTRAINT ck_usuario_perfil CHECK (perfil IN ('CIDADAO', 'FUNCIONARIO', 'ADMIN')),
    cpf             VARCHAR(14)   NULL,                      -- só cidadão
    saldo_ecocoins  INT           NOT NULL CONSTRAINT df_usuario_saldo DEFAULT 0
                    CONSTRAINT ck_usuario_saldo CHECK (saldo_ecocoins >= 0),
    matricula       VARCHAR(20)   NULL,                      -- funcionário / admin
    ponto_coleta_id BIGINT        NULL CONSTRAINT fk_usuario_ponto REFERENCES dbo.ponto_coleta (id)
);
GO

-- CPF único, mas só entre quem tem CPF (funcionários e admin não têm)
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ux_usuario_cpf' AND object_id = OBJECT_ID(N'dbo.usuario'))
CREATE UNIQUE INDEX ux_usuario_cpf ON dbo.usuario (cpf) WHERE cpf IS NOT NULL;
GO

IF OBJECT_ID(N'dbo.descarte', N'U') IS NULL
CREATE TABLE dbo.descarte (
    id              BIGINT IDENTITY(1,1) CONSTRAINT pk_descarte PRIMARY KEY,
    cidadao_id      BIGINT        NOT NULL CONSTRAINT fk_descarte_cidadao     REFERENCES dbo.usuario (id),
    funcionario_id  BIGINT        NOT NULL CONSTRAINT fk_descarte_funcionario REFERENCES dbo.usuario (id),
    ponto_coleta_id BIGINT        NOT NULL CONSTRAINT fk_descarte_ponto       REFERENCES dbo.ponto_coleta (id),
    material        VARCHAR(20)   NOT NULL
                    CONSTRAINT ck_descarte_material CHECK (material IN ('PAPEL', 'PLASTICO', 'VIDRO', 'METAL', 'ELETRONICO')),
    peso_kg         DECIMAL(7, 2) NOT NULL CONSTRAINT ck_descarte_peso   CHECK (peso_kg > 0),
    pontos          INT           NOT NULL CONSTRAINT ck_descarte_pontos CHECK (pontos > 0),
    data_hora       DATETIME2(0)  NOT NULL CONSTRAINT df_descarte_data DEFAULT SYSDATETIME()
);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_descarte_cidadao' AND object_id = OBJECT_ID(N'dbo.descarte'))
CREATE INDEX ix_descarte_cidadao ON dbo.descarte (cidadao_id, data_hora DESC);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_descarte_ponto_data' AND object_id = OBJECT_ID(N'dbo.descarte'))
CREATE INDEX ix_descarte_ponto_data ON dbo.descarte (ponto_coleta_id, data_hora DESC);
GO
