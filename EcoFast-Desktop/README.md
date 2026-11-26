# EcoFast Desktop 🌱

Aplicativo de desktop (Java + Swing) para gestão de ecopontos com recompensas em EcoCoins.
Roda como programa normal do Windows (`EcoFast.exe`), sem navegador e sem servidor web.
Os dados ficam em um banco **SQL Server**.

## Como usar o aplicativo pronto

1. Abra `dist\EcoFast\EcoFast.exe` (dois cliques). O Java já vai dentro da pasta.
2. Na primeira vez, se o banco ainda não estiver configurado, aparece a tela **Conexão com o SQL Server**.
   Informe servidor, porta, usuário e senha. O app **cria o banco `EcoFast` e as tabelas sozinho**
   e guarda a configuração em `%APPDATA%\EcoFast\config.properties`.
3. Entre com uma das contas de demonstração (senha `123456`):

| Perfil        | E-mail                    |
|---------------|---------------------------|
| Cidadã        | maria@email.com           |
| Funcionário   | funcionario@ecofast.com   |
| Administrador | admin@ecofast.com         |

CPFs para testar o registro de entrega: `111.111.111-11`, `222.222.222-22`, `333.333.333-33`, `444.444.444-44`.

## Idiomas

O botão **PT | EN** (no alto da tela de login e na barra de cima) troca o idioma na hora, sem perder o que já foi
preenchido. Datas e números acompanham o idioma, e a escolha fica guardada. Os dados do banco (nomes e ecopontos)
não são traduzidos. Cada texto fica nos dois idiomas lado a lado, em `Idioma.t("português", "english")`.

## Requisitos

- Windows 10 ou 11.
- Um SQL Server acessível (o Express, que é grátis, serve) com **TCP/IP habilitado** e **login por usuário e senha** (modo misto).
- Para gerar o `.exe` de novo: JDK 17 ou mais novo.

## Gerar o `.exe`

```powershell
.\empacotar.ps1
```

Compila, junta tudo em um JAR, gera o ícone e monta `dist\EcoFast\` com o Java embutido (usa `jpackage`).

## Estrutura

```
src/main/java/br/com/ecofast/
├─ Main.java                  abre a janela
├─ model/                     classes de domínio (POO)
│  ├─ Usuario (abstrata) ◄── Cidadao, Funcionario ◄── Administrador
│  ├─ Descarte, PontoColeta, Perfil, Regras, Relatorio
│  └─ material/               Material (abstrata) ◄── Papel, Plastico, Vidro, Metal, Eletronico
├─ dao/                       acesso ao SQL Server (JDBC): interface Dao<T> + um DAO por tabela
├─ service/                   regras de negócio: login, registrar descarte, relatórios
├─ infra/                     conexão (Banco), configuração (ConfigBanco), criação do banco (BancoSetup)
├─ excecao/                   exceções próprias
└─ ui/                        telas Swing
   ├─ Tema (cores, fontes, botões), BotaoEco
   ├─ JanelaPrincipal, TelaLogin, DialogoConexao
   ├─ PainelBase (abstrata) ◄── PainelCidadao, PainelFuncionario, PainelRelatorios, PainelAdmin
   ├─ PainelFactory           escolhe o painel pelo perfil de quem entrou
   └─ componentes/            Cartao, FaixaKpi, Regua, TabelaDados, TicketPesagem, GraficoBarras, GraficoMateriais...
src/main/resources/db/
├─ 01-estrutura.sql           tabelas (T-SQL, roda quantas vezes quiser)
└─ 02-dados-exemplo.sql       dados de demonstração (só entram no banco novo)
```

## Visual

O app usa a mesma identidade da página do projeto: papel, tinta verde, ouro, linhas de 2 px, cantos retos,
sombra deslocada só no que merece foco, títulos em caixa-alta condensada e números em fonte monoespaçada.
As fontes são as que o Windows já traz (Bahnschrift, Segoe UI e Consolas), então não há arquivo de fonte para instalar.
Tudo fica em `ui/Tema.java` e `ui/BotaoEco.java`; os blocos de tela ficam em `ui/componentes/`.

## Onde estão os pilares de POO

- **Herança:** `Cidadao` e `Funcionario` herdam de `Usuario`; `Administrador` herda de `Funcionario`.
  Na tela, `PainelCidadao`, `PainelFuncionario` e `PainelRelatorios` herdam de `PainelBase`.
- **Polimorfismo:** `Descarte.calcularPontos()` chama `material.calcularPontos(peso)` e cada subclasse de
  `Material` responde do seu jeito (o `Eletronico` ainda soma um bônus). `PainelFactory` abre um painel
  diferente para cada `Perfil`, e os DAOs cumprem a mesma interface `Dao<T>`.
- **Encapsulamento:** atributos `private`; o saldo do `Cidadao` não tem setter e só muda por `creditar()`;
  o construtor de `Descarte` valida o peso; a senha é guardada como hash SHA-256.
- **Abstração:** `Usuario`, `Material` e `PainelBase` são classes abstratas; `Dao<T>` é uma interface.

## Regras do programa

- A cada 1.000 EcoCoins, 10% de desconto no IPTU, proporcional (100 = 1%), máximo de 100%.
- Só o funcionário registra a entrega, depois de pesar. O cidadão apenas consulta.
- Pontos por quilo (valores de exemplo): papel 5, vidro 8, plástico 10, metal 15, eletrônico 25 (+20 por entrega).
- A gravação da entrega (descarte + saldo) é uma única transação, e a linha do cidadão é travada
  (`UPDLOCK`) durante ela, então duas entregas ao mesmo tempo não perdem pontos.

## Testes

```powershell
mvn test
```

- `RegrasDeNegocioTest`: regras de POO, sem banco.
- `BancoSqlServerTest`: roda contra o SQL Server de verdade, num banco temporário `EcoFast_Teste` (apagado no fim).
  Sem SQL Server configurado neste computador, esses testes são pulados.

## Observação sobre a senha do banco

A senha de conexão fica em texto simples em `%APPDATA%\EcoFast\config.properties`, uma pasta do seu usuário
do Windows. Para um projeto acadêmico isso basta; num sistema de produção o certo seria cofre de senhas
ou autenticação integrada do Windows.
