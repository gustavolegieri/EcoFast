package br.com.ecofast.infra;

import br.com.ecofast.Idioma;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;

/**
 * Prepara o SQL Server na primeira abertura: cria o banco de dados se ele não
 * existir, cria as tabelas e, só quando as tabelas são novas, insere os dados
 * de exemplo. Nas aberturas seguintes não mexe em nada.
 */
public final class BancoSetup {

    private static final Pattern NOME_SEGURO = Pattern.compile("[A-Za-z0-9_]{1,60}");
    private static final Pattern LINHA_GO = Pattern.compile("(?im)^\\s*GO\\s*$");
    /** Erro do SQL Server para "o banco informado não existe ou o login não entra nele". */
    private static final int ERRO_BANCO_NAO_ABRE = 4060;

    private BancoSetup() {
    }

    public static void garantir(ConfigBanco cfg) throws SQLException {
        if (!NOME_SEGURO.matcher(cfg.banco()).matches()) {
            throw new IllegalArgumentException(Idioma.t("Nome do banco inválido: use só letras, números e _.", "Invalid database name: use only letters, numbers and _."));
        }
        try (Connection c = abrir(cfg)) {
            prepararEstrutura(c);
            return;
        } catch (SQLException e) {
            if (e.getErrorCode() != ERRO_BANCO_NAO_ABRE) {
                throw e;
            }
        }
        criarBanco(cfg);
        try (Connection c = abrir(cfg)) {
            prepararEstrutura(c);
        }
    }

    private static Connection abrir(ConfigBanco cfg) throws SQLException {
        return DriverManager.getConnection(cfg.url(), cfg.usuario(), cfg.senha());
    }

    private static void criarBanco(ConfigBanco cfg) throws SQLException {
        ConfigBanco master = cfg.comBanco("master");
        try (Connection c = abrir(master); Statement st = c.createStatement()) {
            st.execute("IF DB_ID(N'" + cfg.banco() + "') IS NULL CREATE DATABASE [" + cfg.banco() + "]");
        }
    }

    private static void prepararEstrutura(Connection c) throws SQLException {
        boolean novo = !tabelaExiste(c, "usuario");
        executarScript(c, "/db/01-estrutura.sql");
        if (novo) {
            executarScript(c, "/db/02-dados-exemplo.sql");
        }
    }

    private static boolean tabelaExiste(Connection c, String tabela) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT OBJECT_ID(N'dbo." + tabela + "', N'U')")) {
            return rs.next() && rs.getObject(1) != null;
        }
    }

    private static void executarScript(Connection c, String recurso) throws SQLException {
        String script;
        try (InputStream in = BancoSetup.class.getResourceAsStream(recurso)) {
            if (in == null) {
                throw new IllegalStateException(Idioma.t("Script não encontrado: ", "Script not found: ") + recurso);
            }
            script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(Idioma.t("Não consegui ler ", "Couldn't read ") + recurso, e);
        }
        for (String lote : LINHA_GO.split(script)) {
            if (lote.isBlank()) {
                continue;
            }
            try (Statement st = c.createStatement()) {
                st.execute(lote);
            }
        }
    }
}
