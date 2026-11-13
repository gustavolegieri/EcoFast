package br.com.ecofast.model;

/**
 * Administrador do ecoponto. É um Funcionario (herda tudo dele, inclusive
 * poder registrar descartes) e ainda acessa o painel de relatórios.
 */
public class Administrador extends Funcionario {

    public Administrador(Long id, String nome, String email, String senhaHash, String matricula, Long pontoColetaId) {
        super(id, nome, email, senhaHash, matricula, pontoColetaId);
    }

    @Override
    public Perfil getPerfil() {
        return Perfil.ADMIN;
    }

    @Override
    public boolean podeVerRelatorios() {
        return true;
    }
}
