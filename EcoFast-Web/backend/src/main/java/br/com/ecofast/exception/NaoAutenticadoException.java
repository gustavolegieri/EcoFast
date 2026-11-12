package br.com.ecofast.exception;

/** Ninguém logado ou login inválido (vira HTTP 401). */
public class NaoAutenticadoException extends RuntimeException {

    public NaoAutenticadoException(String mensagem) {
        super(mensagem);
    }
}
