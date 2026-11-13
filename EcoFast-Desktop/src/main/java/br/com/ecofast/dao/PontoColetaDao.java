package br.com.ecofast.dao;

import br.com.ecofast.model.PontoColeta;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class PontoColetaDao extends DaoBase implements Dao<PontoColeta> {

    public PontoColetaDao(Connection conexao) {
        super(conexao);
    }

    private static PontoColeta mapear(ResultSet rs) throws SQLException {
        return new PontoColeta(rs.getLong("id"), rs.getString("nome"), rs.getString("endereco"));
    }

    @Override
    public Optional<PontoColeta> buscarPorId(long id) throws SQLException {
        return consultar("SELECT id, nome, endereco FROM dbo.ponto_coleta WHERE id = ?", PontoColetaDao::mapear, id)
                .stream().findFirst();
    }

    @Override
    public List<PontoColeta> listarTodos() throws SQLException {
        return consultar("SELECT id, nome, endereco FROM dbo.ponto_coleta ORDER BY nome", PontoColetaDao::mapear);
    }
}
