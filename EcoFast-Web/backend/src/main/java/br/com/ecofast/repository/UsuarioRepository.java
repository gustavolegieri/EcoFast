package br.com.ecofast.repository;

import br.com.ecofast.model.Administrador;
import br.com.ecofast.model.Cidadao;
import br.com.ecofast.model.Funcionario;
import br.com.ecofast.model.Usuario;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Todos os perfis ficam na mesma tabela "usuario" (coluna "perfil" diz o tipo).
 * Ao ler do banco, o RowMapper instancia a subclasse correta: Cidadao,
 * Funcionario ou Administrador.
 */
@Repository
public class UsuarioRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<Usuario> MAPPER = (rs, n) -> {
        long id = rs.getLong("id");
        String nome = rs.getString("nome");
        String email = rs.getString("email");
        String hash = rs.getString("senha_hash");
        Long ponto = rs.getObject("ponto_coleta_id") == null ? null : rs.getLong("ponto_coleta_id");
        return switch (rs.getString("perfil")) {
            case "CIDADAO" -> new Cidadao(id, nome, email, hash, rs.getString("cpf"), rs.getInt("saldo_ecocoins"));
            case "FUNCIONARIO" -> new Funcionario(id, nome, email, hash, rs.getString("matricula"), ponto);
            case "ADMIN" -> new Administrador(id, nome, email, hash, rs.getString("matricula"), ponto);
            default -> throw new IllegalStateException("Perfil inválido no banco: " + rs.getString("perfil"));
        };
    };

    public UsuarioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return jdbc.query("SELECT * FROM usuario WHERE email = ?", MAPPER, email).stream().findFirst();
    }

    public Optional<Usuario> buscarPorId(long id) {
        return jdbc.query("SELECT * FROM usuario WHERE id = ?", MAPPER, id).stream().findFirst();
    }

    public Optional<Cidadao> buscarCidadaoPorCpf(String cpf) {
        return jdbc.query("SELECT * FROM usuario WHERE perfil = 'CIDADAO' AND cpf = ?", MAPPER, cpf)
                .stream().map(Cidadao.class::cast).findFirst();
    }

    public List<Cidadao> listarCidadaos() {
        return jdbc.query("SELECT * FROM usuario WHERE perfil = 'CIDADAO' ORDER BY nome", MAPPER)
                .stream().map(Cidadao.class::cast).toList();
    }

    /** Persiste o saldo calculado pelo objeto Cidadao. */
    public void atualizarSaldo(Cidadao cidadao) {
        jdbc.update("UPDATE usuario SET saldo_ecocoins = ? WHERE id = ?", cidadao.getSaldoEcoCoins(), cidadao.getId());
    }
}
