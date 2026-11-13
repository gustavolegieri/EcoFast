package br.com.ecofast.service;

import br.com.ecofast.model.Usuario;

/** Quem está usando o aplicativo agora. Substitui a sessão do navegador da versão web. */
public class SessaoUsuario {

    private Usuario usuario;

    public void iniciar(Usuario novo) {
        this.usuario = novo;
    }

    public void encerrar() {
        this.usuario = null;
    }

    public boolean estaLogado() {
        return usuario != null;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}
