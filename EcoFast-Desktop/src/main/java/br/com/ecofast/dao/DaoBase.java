package br.com.ecofast.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Parte repetitiva do JDBC (preparar, preencher parâmetros, ler linhas) fica aqui. */
abstract class DaoBase {

    /** Transforma a linha atual do resultado em um objeto. */
    @FunctionalInterface
    protected interface Mapeador<R> {
        R mapear(ResultSet rs) throws SQLException;
    }

    protected final Connection conexao;

    protected DaoBase(Connection conexao) {
        this.conexao = conexao;
    }

    protected <R> List<R> consultar(String sql, Mapeador<R> mapeador, Object... parametros) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            preencher(ps, parametros);
            try (ResultSet rs = ps.executeQuery()) {
                List<R> linhas = new ArrayList<>();
                while (rs.next()) {
                    linhas.add(mapeador.mapear(rs));
                }
                return linhas;
            }
        }
    }

    protected int executar(String sql, Object... parametros) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            preencher(ps, parametros);
            return ps.executeUpdate();
        }
    }

    protected static void preencher(PreparedStatement ps, Object... parametros) throws SQLException {
        for (int i = 0; i < parametros.length; i++) {
            ps.setObject(i + 1, parametros[i]);
        }
    }
}
