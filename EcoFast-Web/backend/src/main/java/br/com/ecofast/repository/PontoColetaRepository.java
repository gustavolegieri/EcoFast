package br.com.ecofast.repository;

import br.com.ecofast.model.PontoColeta;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PontoColetaRepository {

    private final JdbcTemplate jdbc;

    public PontoColetaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<PontoColeta> buscarPorId(long id) {
        return jdbc.query("SELECT * FROM ponto_coleta WHERE id = ?",
                        (rs, n) -> new PontoColeta(rs.getLong("id"), rs.getString("nome"), rs.getString("endereco")), id)
                .stream().findFirst();
    }
}
