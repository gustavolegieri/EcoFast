package br.com.ecofast.excecao;

import java.sql.SQLException;

/** Falha ao falar com o SQL Server, já com uma mensagem que o usuário entende. */
public class BancoException extends RuntimeException {

    public BancoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

    public static BancoException de(SQLException e) {
        return new BancoException(traduzir(e), e);
    }

    private static String traduzir(SQLException e) {
        String msg = e.getMessage() == null ? "" : e.getMessage();
        if (e.getErrorCode() == 18456 || msg.contains("Login failed")) {
            return "Usuário ou senha do SQL Server inválidos.";
        }
        if (msg.contains("TCP/IP connection") || msg.contains("Connection refused")
                || msg.contains("connection to the host") || msg.contains("instance specified")) {
            return "Não consegui conectar ao servidor SQL Server. Confira o endereço e a porta, "
                    + "se o serviço está ligado e se o protocolo TCP/IP está habilitado.";
        }
        if (e.getErrorCode() == 262 || e.getErrorCode() == 229 || e.getErrorCode() == 916
                || msg.toLowerCase().contains("permission")) {
            return "Esse usuário não tem permissão para criar ou usar o banco de dados.";
        }
        int quebra = msg.indexOf('\n');
        return quebra > 0 ? msg.substring(0, quebra) : msg;
    }
}
