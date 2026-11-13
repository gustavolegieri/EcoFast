package br.com.ecofast.infra;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

/**
 * Dados de conexão com o SQL Server. Ficam salvos em
 * %APPDATA%\EcoFast\config.properties, e a tela de conexão grava esse arquivo.
 *
 * O "servidor" aceita o formato host ou host\INSTANCIA (ex.: localhost\SQLEXPRESS).
 */
public record ConfigBanco(String servidor, int porta, String banco, String usuario, String senha) {

    public static final String BANCO_PADRAO = "EcoFast";

    public static ConfigBanco padrao() {
        return new ConfigBanco("localhost", 1433, BANCO_PADRAO, "sa", "");
    }

    public static Path arquivo() {
        String base = System.getenv("APPDATA");
        Path pasta = base != null ? Path.of(base) : Path.of(System.getProperty("user.home"));
        return pasta.resolve("EcoFast").resolve("config.properties");
    }

    /** Lê o arquivo salvo; vazio se o app nunca foi configurado neste computador. */
    public static Optional<ConfigBanco> carregar() {
        Path arq = arquivo();
        if (!Files.isRegularFile(arq)) {
            return Optional.empty();
        }
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(arq)) {
            p.load(in);
        } catch (IOException e) {
            return Optional.empty();
        }
        ConfigBanco base = padrao();
        int porta;
        try {
            porta = Integer.parseInt(p.getProperty("porta", "1433").trim());
        } catch (NumberFormatException e) {
            porta = base.porta();
        }
        return Optional.of(new ConfigBanco(
                p.getProperty("servidor", base.servidor()).trim(),
                porta,
                p.getProperty("banco", base.banco()).trim(),
                p.getProperty("usuario", base.usuario()).trim(),
                p.getProperty("senha", "")));
    }

    public void salvar() throws IOException {
        Path arq = arquivo();
        Files.createDirectories(arq.getParent());
        Properties p = new Properties();
        p.setProperty("servidor", servidor);
        p.setProperty("porta", String.valueOf(porta));
        p.setProperty("banco", banco);
        p.setProperty("usuario", usuario);
        p.setProperty("senha", senha);
        try (OutputStream out = Files.newOutputStream(arq)) {
            p.store(out, "Conexao do EcoFast com o SQL Server");
        }
    }

    public ConfigBanco comBanco(String outroBanco) {
        return new ConfigBanco(servidor, porta, outroBanco, usuario, senha);
    }

    /** Endereço JDBC. O usuário e a senha vão separados, nunca dentro da URL. */
    public String url() {
        String host = servidor.trim();
        String instancia = null;
        int barra = host.indexOf('\\');
        if (barra >= 0) {
            instancia = host.substring(barra + 1).trim();
            host = host.substring(0, barra).trim();
        }
        StringBuilder u = new StringBuilder("jdbc:sqlserver://").append(host);
        if (instancia != null && !instancia.isEmpty()) {
            u.append(";instanceName=").append(instancia);
        } else {
            u.append(':').append(porta);
        }
        u.append(";databaseName=").append(banco)
                .append(";encrypt=true;trustServerCertificate=true;loginTimeout=8");
        return u.toString();
    }
}
