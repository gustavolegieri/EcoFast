package br.com.ecofast.repository;

import br.com.ecofast.model.Descarte;
import br.com.ecofast.model.material.MaterialFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class DescarteRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<Descarte> MAPPER = (rs, n) -> new Descarte(
            rs.getLong("id"),
            rs.getLong("cidadao_id"),
            rs.getLong("funcionario_id"),
            rs.getLong("ponto_coleta_id"),
            MaterialFactory.criar(rs.getString("material")),
            rs.getDouble("peso_kg"),
            rs.getInt("pontos"),
            rs.getTimestamp("data_hora").toLocalDateTime());

    public DescarteRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Descarte salvar(Descarte d) {
        GeneratedKeyHolder chave = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO descarte (cidadao_id, funcionario_id, ponto_coleta_id, material, peso_kg, pontos, data_hora) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                    new String[] {"id"});
            ps.setLong(1, d.getCidadaoId());
            ps.setLong(2, d.getFuncionarioId());
            ps.setLong(3, d.getPontoColetaId());
            ps.setString(4, d.getMaterial().getTipo());
            ps.setDouble(5, d.getPesoKg());
            ps.setInt(6, d.getPontos());
            ps.setTimestamp(7, Timestamp.valueOf(d.getDataHora()));
            return ps;
        }, chave);
        d.setId(chave.getKey().longValue());
        return d;
    }

    public List<Descarte> listarPorCidadao(long cidadaoId) {
        return jdbc.query("SELECT * FROM descarte WHERE cidadao_id = ? ORDER BY data_hora DESC", MAPPER, cidadaoId);
    }

    /** Últimos registros feitos em um ecoponto, com o nome do cidadão. */
    public List<Map<String, Object>> ultimosDoPonto(long pontoColetaId, int limite) {
        return jdbc.queryForList(
                "SELECT d.id, u.nome AS cidadao, d.material, d.peso_kg, d.pontos, d.data_hora "
                        + "FROM descarte d JOIN usuario u ON u.id = d.cidadao_id "
                        + "WHERE d.ponto_coleta_id = ? ORDER BY d.data_hora DESC LIMIT ?",
                pontoColetaId, limite);
    }

    // ---------- Consultas do painel do administrador ----------

    public Map<String, Object> resumoDesde(LocalDateTime inicio) {
        return jdbc.queryForMap(
                "SELECT COUNT(*) AS entregas, COALESCE(SUM(peso_kg), 0) AS total_kg, "
                        + "COALESCE(SUM(pontos), 0) AS total_pontos, COUNT(DISTINCT cidadao_id) AS cidadaos "
                        + "FROM descarte WHERE data_hora >= ?",
                Timestamp.valueOf(inicio));
    }

    public List<Map<String, Object>> kgPorMaterialDesde(LocalDateTime inicio) {
        return jdbc.queryForList(
                "SELECT material, SUM(peso_kg) AS total_kg FROM descarte WHERE data_hora >= ? "
                        + "GROUP BY material ORDER BY total_kg DESC",
                Timestamp.valueOf(inicio));
    }

    public List<Map<String, Object>> kgPorDiaDesde(LocalDateTime inicio) {
        return jdbc.queryForList(
                "SELECT CAST(data_hora AS DATE) AS dia, SUM(peso_kg) AS total_kg FROM descarte "
                        + "WHERE data_hora >= ? GROUP BY CAST(data_hora AS DATE) ORDER BY dia",
                Timestamp.valueOf(inicio));
    }

    public List<Map<String, Object>> rankingDesde(LocalDateTime inicio, int limite) {
        return jdbc.queryForList(
                "SELECT u.nome, SUM(d.peso_kg) AS total_kg, SUM(d.pontos) AS pontos "
                        + "FROM descarte d JOIN usuario u ON u.id = d.cidadao_id WHERE d.data_hora >= ? "
                        + "GROUP BY u.nome ORDER BY pontos DESC LIMIT ?",
                Timestamp.valueOf(inicio), limite);
    }
}
