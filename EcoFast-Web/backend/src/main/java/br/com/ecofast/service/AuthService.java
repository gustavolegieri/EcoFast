package br.com.ecofast.service;

import br.com.ecofast.exception.NaoAutenticadoException;
import br.com.ecofast.model.Usuario;
import br.com.ecofast.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

/** Login, logout e "quem está logado nesta sessão do navegador". */
@Service
public class AuthService {

    private static final String CHAVE_SESSAO = "usuarioId";

    private final UsuarioRepository usuarios;

    public AuthService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public Usuario login(String email, String senha, HttpSession sessao) {
        Usuario usuario = usuarios.buscarPorEmail(email == null ? "" : email.trim().toLowerCase())
                .filter(u -> u.autenticar(senha))
                .orElseThrow(() -> new NaoAutenticadoException("E-mail ou senha inválidos."));
        sessao.setAttribute(CHAVE_SESSAO, usuario.getId());
        return usuario;
    }

    public void logout(HttpSession sessao) {
        sessao.invalidate();
    }

    public Usuario usuarioLogado(HttpSession sessao) {
        Object id = sessao.getAttribute(CHAVE_SESSAO);
        if (id == null) {
            throw new NaoAutenticadoException("Faça login para continuar.");
        }
        return usuarios.buscarPorId((Long) id)
                .orElseThrow(() -> new NaoAutenticadoException("Sessão expirada. Faça login novamente."));
    }
}
