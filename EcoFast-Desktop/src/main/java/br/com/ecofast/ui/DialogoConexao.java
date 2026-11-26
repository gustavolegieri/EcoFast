package br.com.ecofast.ui;

import br.com.ecofast.Idioma;
import br.com.ecofast.infra.BancoSetup;
import br.com.ecofast.infra.ConfigBanco;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import java.awt.Font;
import java.awt.Frame;
import java.util.Optional;

/**
 * Tela de conexão com o SQL Server. Testa de verdade (e cria o banco e as tabelas
 * se faltarem) antes de salvar, então se ela fechar com sucesso o app já pode usar.
 */
public final class DialogoConexao extends JDialog {

    private final JTextField servidor = new JTextField();
    private final JTextField porta = new JTextField();
    private final JTextField banco = new JTextField();
    private final JTextField usuario = new JTextField();
    private final JPasswordField senha = new JPasswordField();
    private final JLabel mensagem = new JLabel(" ");
    private final JButton conectar = Tema.botaoPrimario(Idioma.t("Testar e salvar", "Test and save"));
    private final JButton cancelar = Tema.botaoSecundario(Idioma.t("Cancelar", "Cancel"));
    private ConfigBanco resultado;

    public DialogoConexao(Frame dono, ConfigBanco inicial, String aviso) {
        super(dono, Idioma.t("Conexão com o SQL Server", "SQL Server connection"), true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setIconImages(Icone.imagens());

        servidor.setText(inicial.servidor());
        porta.setText(String.valueOf(inicial.porta()));
        banco.setText(inicial.banco());
        usuario.setText(inicial.usuario());
        senha.setText(inicial.senha());
        servidor.setName("dlgServidor");
        porta.setName("dlgPorta");
        banco.setName("dlgBanco");
        usuario.setName("dlgUsuario");
        senha.setName("dlgSenha");
        conectar.setName("dlgConectar");

        JPanel p = new JPanel(new MigLayout("insets 26 30 22 30, wrap 2, fillx", "[grow, fill, 300!][grow, fill, 110!]", ""));
        p.setBackground(Tema.PAPEL);
        p.add(Tema.kicker(Idioma.t("Banco de dados", "Database"), Tema.VERDE), "span 2");
        p.add(Tema.titulo(Idioma.t("Conexão com o SQL Server", "SQL Server connection"), 34, Tema.TINTA), "span 2, gaptop 6");
        p.add(Tema.rotulo("<html><div style='width:300px'>" + Idioma.t(
                "O EcoFast guarda tudo em um banco SQL Server. Informe onde ele está. Se o banco ainda não existir, ele é criado agora.",
                "EcoFast stores everything in a SQL Server database. Tell it where it is. If the database doesn't exist yet, it is created now.")
                + "</div></html>", 15, Font.PLAIN, Tema.TINTA_SUAVE), "span 2, gaptop 8, gapbottom 14");

        p.add(Tema.nomeDoCampo(Idioma.t("Servidor", "Server")));
        p.add(Tema.nomeDoCampo(Idioma.t("Porta", "Port")));
        p.add(servidor);
        p.add(porta);
        p.add(Tema.rotulo(Idioma.t("Exemplos: localhost  ou  MEU-PC\\SQLEXPRESS", "Examples: localhost  or  MY-PC\\SQLEXPRESS"), 13, Font.PLAIN, Tema.TINTA_SUAVE), "span 2, gapbottom 8");

        p.add(Tema.nomeDoCampo(Idioma.t("Banco de dados", "Database")), "span 2");
        p.add(banco, "span 2, gapbottom 6");
        p.add(Tema.nomeDoCampo(Idioma.t("Usuário", "Username")), "span 2");
        p.add(usuario, "span 2, gapbottom 6");
        p.add(Tema.nomeDoCampo(Idioma.t("Senha", "Password")), "span 2");
        p.add(senha, "span 2");

        mensagem.setFont(Tema.fonte(14, Font.BOLD));
        mensagem.setName("dlgMensagem");
        p.add(mensagem, "span 2, gaptop 10");
        if (aviso != null && !aviso.isEmpty()) {
            definirMensagem(aviso, true);
        }

        JPanel botoes = new JPanel(new MigLayout("insets 0", "[grow][][]"));
        botoes.setOpaque(false);
        botoes.add(new JLabel(), "growx");
        botoes.add(cancelar);
        botoes.add(conectar);
        p.add(botoes, "span 2, gaptop 14, growx");

        cancelar.addActionListener(e -> dispose());
        conectar.addActionListener(e -> testarESalvar());
        for (JTextField campo : new JTextField[]{servidor, porta, banco, usuario, senha}) {
            campo.addActionListener(e -> conectar.doClick());
        }

        setContentPane(p);
        pack();
        setResizable(false);
        setLocationRelativeTo(dono);
    }

    public Optional<ConfigBanco> resultado() {
        return Optional.ofNullable(resultado);
    }

    private void definirMensagem(String texto, boolean ehErro) {
        mensagem.setForeground(ehErro ? Tema.ALERTA : Tema.VERDE);
        mensagem.setText("<html><div style='width:430px'>" + texto + "</div></html>");
        pack();
    }

    private void testarESalvar() {
        int numeroPorta;
        try {
            numeroPorta = Integer.parseInt(porta.getText().trim());
        } catch (NumberFormatException e) {
            definirMensagem(Idioma.t("A porta precisa ser um número (o normal é 1433).", "The port must be a number (usually 1433)."), true);
            return;
        }
        ConfigBanco cfg = new ConfigBanco(servidor.getText().trim(), numeroPorta, banco.getText().trim(),
                usuario.getText().trim(), new String(senha.getPassword()));
        conectar.setEnabled(false);
        definirMensagem(Idioma.t("Conectando…", "Connecting…"), false);
        Tarefa.executar(() -> {
            BancoSetup.garantir(cfg);
            cfg.salvar();
            return cfg;
        }, ok -> {
            resultado = ok;
            dispose();
        }, msg -> {
            conectar.setEnabled(true);
            definirMensagem(msg, true);
        });
    }
}
