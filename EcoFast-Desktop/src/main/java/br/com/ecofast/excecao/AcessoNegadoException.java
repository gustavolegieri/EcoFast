package br.com.ecofast.excecao;

/** Usuário logado, mas sem permissão para a ação. */
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
