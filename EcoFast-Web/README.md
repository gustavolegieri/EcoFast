# EcoFast 🌱

Plataforma web de gestão de ecopontos com recompensas (EcoCoins).
Front-end em HTML/CSS, back-end em Java (Spring Boot) e banco SQL.

## Estrutura

```
EcoFast-Web/
├── apresentacao/        Página do pitch (index.html, em inglês) + img/ com as telas do sistema
├── frontend/            Telas HTML/CSS/JS (o "balcão de atendimento")
│   ├── index.html         login
│   ├── funcionario.html   registrar descarte (funcionário/admin)
│   ├── cidadao.html       extrato e saldo (cidadão)
│   ├── admin.html         relatórios com gráficos (admin)
│   ├── css/style.css
│   └── js/                api.js, login.js, funcionario.js, cidadao.js, admin.js
├── backend/             Projeto Java / Maven (o "cérebro")
│   └── src/main/java/br/com/ecofast/
│       ├── model/           Usuario (abstrata), Cidadao, Funcionario, Administrador,
│       │                    PontoColeta, Descarte
│       ├── model/material/  Material (abstrata), Papel, Plastico, Vidro, Metal,
│       │                    Eletronico, MaterialFactory
│       ├── controller/      Endpoints REST que recebem os formulários
│       ├── service/         Regras de negócio (login, descarte, relatórios)
│       ├── repository/      SQL com JdbcTemplate
│       └── exception/       Erros -> respostas JSON (400/401/403)
└── banco/ecofast.sql    Script para MySQL/MariaDB (XAMPP)
```

## Como rodar

Requisitos: Java 17+ e Maven.

```bash
cd backend
mvn spring-boot:run
```

Abra http://localhost:8080. Usa banco H2 em memória com dados de exemplo (recriados a cada execução).

### Usando o MySQL do XAMPP

1. Importe `banco/ecofast.sql` no phpMyAdmin.
2. Rode: `mvn spring-boot:run -Dspring-boot.run.profiles=mysql`
   (usuário `root` sem senha; ajuste em `backend/src/main/resources/application-mysql.properties`).

### Contas de demonstração (senha `123456`)

| Perfil        | E-mail                    |
|---------------|---------------------------|
| Cidadã        | maria@email.com           |
| Funcionário   | funcionario@ecofast.com   |
| Administrador | admin@ecofast.com         |

CPFs para testar o registro: `111.111.111-11`, `222.222.222-22`, `333.333.333-33`, `444.444.444-44`.

### Testes

```bash
cd backend
mvn test
```

## Onde estão os pilares de POO

- **Herança:** `Cidadao` e `Funcionario` herdam de `Usuario`; `Administrador` herda de `Funcionario`.
- **Polimorfismo:** `Descarte.calcularPontos()` chama `material.calcularPontos(peso)`; cada subclasse de
  `Material` tem seu multiplicador e `Eletronico` sobrescreve o método com bônus. `getPaginaInicial()`
  leva cada perfil a um painel diferente.
- **Encapsulamento:** atributos `private`; o saldo de `Cidadao` não tem setter e só muda por `creditar()`;
  o construtor de `Descarte` valida o peso; a senha é guardada como hash SHA-256.
- **Abstração:** `Usuario` e `Material` são classes abstratas.

## Pontos por material

| Material        | EcoCoins/kg |
|-----------------|-------------|
| Papel           | 5           |
| Vidro           | 8           |
| Plástico        | 10          |
| Metal           | 15          |
| Eletrônico      | 25 (+20 de bônus por entrega) |

## Apresentação

Abra `apresentacao/index.html` no navegador: página única (em inglês) para o pitch de 5 minutos,
é só rolar. Os nomes da equipe ficam no rodapé.
