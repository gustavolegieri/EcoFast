package br.com.ecofast.ui;

import br.com.ecofast.excecao.AcessoNegadoException;
import br.com.ecofast.excecao.BancoException;
import br.com.ecofast.excecao.NaoAutenticadoException;

import javax.swing.SwingWorker;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * Roda o trabalho pesado (falar com o banco) fora da thread da tela, para a janela
 * nunca travar, e devolve o resultado ou a mensagem de erro de volta na tela.
 */
public final class Tarefa {

    private Tarefa() {
    }

    public static <T> void executar(Callable<T> trabalho, Consumer<T> aoTerminar, Consumer<String> aoFalhar) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return trabalho.call();
            }

            @Override
            protected void done() {
                try {
                    aoTerminar.accept(get());
                } catch (ExecutionException e) {
                    aoFalhar.accept(mensagem(e.getCause()));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    /** Erros esperados viram a própria mensagem; o resto vira um aviso genérico. */
    public static String mensagem(Throwable t) {
        if (t instanceof BancoException || t instanceof IllegalArgumentException
                || t instanceof AcessoNegadoException || t instanceof NaoAutenticadoException) {
            return t.getMessage();
        }
        if (t instanceof SQLException e) {
            return BancoException.de(e).getMessage();
        }
        t.printStackTrace();
        return "Algo inesperado aconteceu: " + (t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage());
    }
}
