package br.com.ecofast.dao;

import br.com.ecofast.model.Administrador;
import br.com.ecofast.model.Cidadao;
import br.com.ecofast.model.Funcionario;
import br.com.ecofast.model.Perfil;
import br.com.ecofast.model.Usuario;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Todos os perfis ficam na mesma tabela "usuario" (a coluna "perfil" diz o tipo).
 * Ao ler do banco, o mapeador instancia a subclasse certa: Cidadao, Funcionario
 * ou Administrador.
 */
public class UsuarioDao extends DaoBase implements Dao<Usuario> {

    private static final String COLUNAS =
            "id, nome, email, senha_hash, perfil, cpf, saldo_ecocoins, matricula, ponto_coleta_id";

    public UsuarioDao(Connection conexao) {
        super(conexao);
    }

    private static Usuario mapear(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String nome = rs.getString("nome");
        String email = rs.getString("email");
        String hash = rs.getString("senha_hash");
        long ponto = rs.getLong("ponto_coleta_id");
        Long pontoId = rs.wasNull() ? null : ponto;
        return switch (Perfil.valueOf(rs.getString("perfil"))) {
            case CIDADAO -> new Cidadao(id, nome, email, hash, rs.getString("cpf"), rs.getInt("saldo_ecocoins"));
            case FUNCIONARIO -> new Funcionario(id, nome, email, hash, rs.getString("matricula"), pontoId);
            case ADMIN -> new Administrador(id, nome, email, hash, rs.getString("matricula"), pontoId);
        };
    }

    @Override
    public Optional<Usuario> buscarPorId(long id) throws SQLException {
        return consultar("SELECT " + COLUNAS + " FROM dbo.usuario WHERE id = ?", UsuarioDao::mapear, id)
                .stream().findFirst();
    }

    @Override
    public List<Usuario> listarTodos() throws SQLException {
        return consultar("SELECT " + COLUNAS + " FROM dbo.usuario ORDER BY nome", UsuarioDao::mapear);
    }

    public Optional<Usuario> buscarPorEmail(String email) throws SQLException {
        return consultar("SELECT " + COLUNAS + " FROM dbo.usuario WHERE email = ?", UsuarioDao::mapear, email)
                .stream().findFirst();
    }

    public Optional<Cidadao> buscarCidadaoPorCpf(String cpf) throws SQLException {
        return buscarCidadao(cpf, "");
    }

    /**
     * Mesma busca, mas trava a linha até o fim da transação. Duas entregas
     * registradas ao mesmo tempo para o mesmo cidadão não se atropelam.
     */
    public Optional<Cidadao> buscarCidadaoPorCpfParaAtualizar(String cpf) throws SQLException {
        return buscarCidadao(cpf, " WITH (UPDLOCK, ROWLOCK)");
    }

    private Optional<Cidadao> buscarCidadao(String cpf, String dica) throws SQLException {
        return consultar("SELECT " + COLUNAS + " FROM dbo.usuario" + dica + " WHERE perfil = 'CIDADAO' AND cpf = ?",
                UsuarioDao::mapear, cpf).stream().map(Cidadao.class::cast).findFirst();
    }

    /** Grava o saldo calculado pelo próprio objeto Cidadao. */
    public void atualizarSaldo(Cidadao cidadao) throws SQLException {
        executar("UPDATE dbo.usuario SET saldo_ecocoins = ? WHERE id = ?", cidadao.getSaldoEcoCoins(), cidadao.getId());
    }
}
