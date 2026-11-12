package br.com.ecofast.exception;

/** Usuário logado, mas sem permissão para a ação (vira HTTP 403). */
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
