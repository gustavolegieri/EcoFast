package br.com.ecofast.service;

import br.com.ecofast.Idioma;
import br.com.ecofast.dao.UsuarioDao;
import br.com.ecofast.excecao.NaoAutenticadoException;
import br.com.ecofast.infra.Banco;
import br.com.ecofast.model.Usuario;

/** Login: confere e-mail e senha no banco e devolve o objeto do perfil certo. */
public class AuthService {

    private final Banco banco;

    public AuthService(Banco banco) {
        this.banco = banco;
    }

    public Usuario login(String email, String senha) {
        String limpo = email == null ? "" : email.trim().toLowerCase();
        if (limpo.isEmpty() || senha == null || senha.isEmpty()) {
            throw new NaoAutenticadoException(Idioma.t("Informe e-mail e senha.", "Enter your email and password."));
        }
        return banco.consultar(c -> new UsuarioDao(c).buscarPorEmail(limpo))
                .filter(u -> u.autenticar(senha))
                .orElseThrow(() -> new NaoAutenticadoException(Idioma.t("E-mail ou senha inválidos.", "Invalid email or password.")));
    }
}
