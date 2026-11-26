package br.com.ecofast.dao;

import br.com.ecofast.Idioma;
import br.com.ecofast.model.Descarte;
import br.com.ecofast.model.RegistroRecente;
import br.com.ecofast.model.Relatorio;
import br.com.ecofast.model.material.MaterialFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class DescarteDao extends DaoBase implements Dao<Descarte> {

    private static final String COLUNAS =
            "id, cidadao_id, funcionario_id, ponto_coleta_id, material, peso_kg, pontos, data_hora";

    public DescarteDao(Connection conexao) {
        super(conexao);
    }

    private static Descarte mapear(ResultSet rs) throws SQLException {
        return new Descarte(
                rs.getLong("id"),
                rs.getLong("cidadao_id"),
                rs.getLong("funcionario_id"),
                rs.getLong("ponto_coleta_id"),
                MaterialFactory.criar(rs.getString("material")),
                rs.getBigDecimal("peso_kg").doubleValue(),
                rs.getInt("pontos"),
                rs.getObject("data_hora", LocalDateTime.class));
    }

    @Override
    public Optional<Descarte> buscarPorId(long id) throws SQLException {
        return consultar("SELECT " + COLUNAS + " FROM dbo.descarte WHERE id = ?", DescarteDao::mapear, id)
                .stream().findFirst();
    }

    @Override
    public List<Descarte> listarTodos() throws SQLException {
        return consultar("SELECT " + COLUNAS + " FROM dbo.descarte ORDER BY data_hora DESC, id DESC",
                DescarteDao::mapear);
    }

    public Descarte salvar(Descarte d) throws SQLException {
        String sql = "INSERT INTO dbo.descarte (cidadao_id, funcionario_id, ponto_coleta_id, material, peso_kg, pontos, data_hora) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencher(ps, d.getCidadaoId(), d.getFuncionarioId(), d.getPontoColetaId(), d.getMaterial().getTipo(),
                    BigDecimal.valueOf(d.getPesoKg()).setScale(2), d.getPontos(), d.getDataHora());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (!chaves.next()) {
                    throw new SQLException(Idioma.t("O banco não devolveu o número do descarte.", "The database did not return the delivery number."));
                }
                d.setId(chaves.getLong(1));
            }
        }
        return d;
    }

    public List<Descarte> listarPorCidadao(long cidadaoId) throws SQLException {
        return consultar("SELECT " + COLUNAS + " FROM dbo.descarte WHERE cidadao_id = ? ORDER BY data_hora DESC, id DESC",
                DescarteDao::mapear, cidadaoId);
    }

    /** Últimos registros feitos em um ecoponto, com o nome do cidadão. */
    public List<RegistroRecente> ultimosDoPonto(long pontoColetaId, int limite) throws SQLException {
        return consultar("SELECT TOP (?) d.id, u.nome AS cidadao, d.material, d.peso_kg, d.pontos, d.data_hora "
                        + "FROM dbo.descarte d JOIN dbo.usuario u ON u.id = d.cidadao_id "
                        + "WHERE d.ponto_coleta_id = ? ORDER BY d.data_hora DESC, d.id DESC",
                rs -> new RegistroRecente(rs.getLong("id"), rs.getString("cidadao"),
                        MaterialFactory.criar(rs.getString("material")), rs.getBigDecimal("peso_kg").doubleValue(),
                        rs.getInt("pontos"), rs.getObject("data_hora", LocalDateTime.class)),
                limite, pontoColetaId);
    }

    // ---------- Consultas do painel do administrador ----------

    public Relatorio.Resumo resumoDesde(LocalDateTime inicio) throws SQLException {
        return consultar("SELECT COUNT(*) AS entregas, ISNULL(SUM(peso_kg), 0) AS total_kg, "
                        + "ISNULL(SUM(CAST(pontos AS BIGINT)), 0) AS total_pontos, COUNT(DISTINCT cidadao_id) AS cidadaos "
                        + "FROM dbo.descarte WHERE data_hora >= ?",
                rs -> new Relatorio.Resumo(rs.getInt("entregas"), rs.getBigDecimal("total_kg").doubleValue(),
                        rs.getLong("total_pontos"), rs.getInt("cidadaos")),
                inicio).get(0);
    }

    public List<Relatorio.PorMaterial> kgPorMaterialDesde(LocalDateTime inicio) throws SQLException {
        return consultar("SELECT material, SUM(peso_kg) AS total_kg FROM dbo.descarte WHERE data_hora >= ? "
                        + "GROUP BY material ORDER BY SUM(peso_kg) DESC",
                rs -> new Relatorio.PorMaterial(MaterialFactory.criar(rs.getString("material")),
                        rs.getBigDecimal("total_kg").doubleValue()),
                inicio);
    }

    public List<Relatorio.PorDia> kgPorDiaDesde(LocalDateTime inicio) throws SQLException {
        return consultar("SELECT CAST(data_hora AS DATE) AS dia, SUM(peso_kg) AS total_kg FROM dbo.descarte "
                        + "WHERE data_hora >= ? GROUP BY CAST(data_hora AS DATE) ORDER BY CAST(data_hora AS DATE)",
                rs -> new Relatorio.PorDia(rs.getObject("dia", LocalDate.class), rs.getBigDecimal("total_kg").doubleValue()),
                inicio);
    }

    public List<Relatorio.Ranking> rankingDesde(LocalDateTime inicio, int limite) throws SQLException {
        return consultar("SELECT TOP (?) u.nome, SUM(d.peso_kg) AS total_kg, SUM(CAST(d.pontos AS BIGINT)) AS pontos "
                        + "FROM dbo.descarte d JOIN dbo.usuario u ON u.id = d.cidadao_id WHERE d.data_hora >= ? "
                        + "GROUP BY u.id, u.nome ORDER BY SUM(CAST(d.pontos AS BIGINT)) DESC",
                rs -> new Relatorio.Ranking(rs.getString("nome"), rs.getBigDecimal("total_kg").doubleValue(),
                        rs.getLong("pontos")),
                limite, inicio);
    }
}
