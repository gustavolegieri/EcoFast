package br.com.ecofast.infra;

import br.com.ecofast.excecao.BancoException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Porta de entrada para o SQL Server. Cada operação abre a sua conexão e a
 * fecha no fim; consultas simples usam consultar(), e o que precisa gravar
 * várias coisas juntas usa emTransacao() (ou tudo é salvo, ou nada).
 */
public final class Banco {

    /** Um trecho de trabalho com a conexão aberta. */
    @FunctionalInterface
    public interface Operacao<T> {
        T executar(Connection conexao) throws SQLException;
    }

    private final ConfigBanco config;

    public Banco(ConfigBanco config) {
        this.config = config;
    }

    public ConfigBanco config() {
        return config;
    }

    public Connection abrir() throws SQLException {
        return DriverManager.getConnection(config.url(), config.usuario(), config.senha());
    }

    public <T> T consultar(Operacao<T> operacao) {
        try (Connection c = abrir()) {
            return operacao.executar(c);
        } catch (SQLException e) {
            throw BancoException.de(e);
        }
    }

    public <T> T emTransacao(Operacao<T> operacao) {
        try (Connection c = abrir()) {
            c.setAutoCommit(false);
            try {
                T resultado = operacao.executar(c);
                c.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw BancoException.de(e);
        }
    }
}
