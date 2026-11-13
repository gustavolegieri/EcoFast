package br.com.ecofast.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Contrato comum de acesso ao banco: cada tabela ganha um DAO que o cumpre. */
public interface Dao<T> {

    Optional<T> buscarPorId(long id) throws SQLException;

    List<T> listarTodos() throws SQLException;
}
