package br.com.ecofast.ui;

import br.com.ecofast.ui.componentes.Bolinha;
import br.com.ecofast.ui.componentes.Cartao;
import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

/** Primeira tela: à esquerda a marca, à direita o formulário de entrada. */
public final class TelaLogin extends JPanel {

    /** O que a tela precisa que a janela faça por ela. */
    public interface Acoes {
        void entrar(String email, String senha);

        void configurarConexao();
    }

    public enum Estado { CONECTANDO, OK, ERRO }

    private final JTextField campoEmail = new JTextField();
    private final JPasswordField campoSenha = new JPasswordField();
    private final JButton botaoEntrar = Tema.botaoPrimario("Entrar");
    private final JLabel erro = new JLabel();
    private final JLabel status = new JLabel();

    public TelaLogin(Acoes acoes) {
        super(new MigLayout("insets 0, fill", "[45%, fill][grow, fill]", "[grow, fill]"));
        add(new PainelMarca());
        add(criarLadoDoFormulario(acoes));
    }

    private JPanel criarLadoDoFormulario(Acoes acoes) {
        JPanel lado = new JPanel(new MigLayout("insets 24, fill", "[grow, center]", "[grow, center]"));
        lado.setBackground(Tema.FUNDO);

        Cartao cartao = new Cartao(new MigLayout("insets 10 14 6 14, wrap 1, fillx, hidemode 3", "[grow, fill]", ""));
        cartao.add(Tema.rotulo("Entrar no EcoFast", 24, Font.BOLD, Tema.VERDE_900));
        cartao.add(Tema.rotulo("Use o e-mail e a senha do seu perfil.", 13, Font.PLAIN, Tema.SUAVE), "gapbottom 14");

        cartao.add(Tema.rotulo("E-mail", 13, Font.BOLD, Tema.TEXTO));
        campoEmail.setName("campoEmail");
        campoEmail.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "voce@email.com");
        cartao.add(campoEmail, "gapbottom 6");

        cartao.add(Tema.rotulo("Senha", 13, Font.BOLD, Tema.TEXTO));
        campoSenha.setName("campoSenha");
        campoSenha.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true");
        cartao.add(campoSenha, "gapbottom 6");

        erro.setForeground(Tema.ERRO);
        erro.setFont(Tema.fonte(13, Font.PLAIN));
        erro.setName("erroLogin");
        erro.setVisible(false);
        cartao.add(erro);

        botaoEntrar.setName("botaoEntrar");
        botaoEntrar.setEnabled(false);
        botaoEntrar.addActionListener(e -> acoes.entrar(campoEmail.getText(), new String(campoSenha.getPassword())));
        campoEmail.addActionListener(e -> campoSenha.requestFocusInWindow());
        campoSenha.addActionListener(e -> botaoEntrar.doClick());
        cartao.add(botaoEntrar, "growx, gaptop 8");

        cartao.add(Tema.rotulo("Contas de demonstração (senha 123456)", 12, Font.PLAIN, Tema.SUAVE), "gaptop 16");
        JPanel demos = new JPanel(new MigLayout("insets 0", "[][][]", "[]"));
        demos.setOpaque(false);
        demos.add(demo("Cidadã", "maria@email.com", "demoCidada"));
        demos.add(demo("Funcionário", "funcionario@ecofast.com", "demoFuncionario"));
        demos.add(demo("Administrador", "admin@ecofast.com", "demoAdmin"));
        cartao.add(demos, "gaptop 0");

        status.setFont(Tema.fonte(12, Font.PLAIN));
        status.setForeground(Tema.SUAVE);
        status.setName("statusConexao");
        JButton conexao = Tema.botaoLink("Conexão com o banco");
        conexao.setName("botaoConexao");
        conexao.addActionListener(e -> acoes.configurarConexao());
        JPanel rodape = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
        rodape.setOpaque(false);
        rodape.add(status);
        rodape.add(conexao);
        cartao.add(rodape, "gaptop 14, growx");

        lado.add(cartao, "w 440!");
        return lado;
    }

    private JButton demo(String texto, String email, String nome) {
        JButton b = Tema.botaoLink(texto);
        b.setName(nome);
        b.addActionListener(e -> {
            campoEmail.setText(email);
            campoSenha.setText("123456");
            campoSenha.requestFocusInWindow();
        });
        return b;
    }

    public void mostrarErro(String texto) {
        erro.setText(texto == null ? "" : "<html><div style='width:360px'>" + texto + "</div></html>");
        erro.setVisible(texto != null && !texto.isEmpty());
    }

    public void liberar(boolean pode) {
        botaoEntrar.setEnabled(pode);
    }

    public void limpar() {
        campoSenha.setText("");
        mostrarErro(null);
    }

    public void focoInicial() {
        (campoEmail.getText().isEmpty() ? campoEmail : campoSenha).requestFocusInWindow();
    }

    public void setStatus(Estado estado, String texto) {
        Color cor = switch (estado) {
            case CONECTANDO -> Tema.MOEDA_COR;
            case OK -> Tema.VERDE_500;
            case ERRO -> Tema.ERRO;
        };
        status.setIcon(new Bolinha(cor, 9));
        status.setIconTextGap(7);
        status.setText(texto);
    }

    /** Lado esquerdo: fundo verde com a promessa do app. */
    private static final class PainelMarca extends JPanel {

        PainelMarca() {
            super(new MigLayout("insets 0 60 0 50, wrap 1, fillx, aligny center", "[grow, fill]", ""));
            setOpaque(false);
            JLabel marca = Tema.rotulo("EcoFast", 28, Font.BOLD, Color.WHITE);
            marca.setIcon(Icone.vetorial(46));
            marca.setIconTextGap(14);
            add(marca, "gapbottom 30");
            add(Tema.rotulo("<html>Recicle no ecoponto.<br>Ganhe EcoCoins.</html>", 40, Font.BOLD, Color.WHITE), "gapbottom 18");
            add(Tema.rotulo("<html><div style='width:360px'>O programa que digitaliza a gestão dos ecopontos da cidade "
                    + "e recompensa quem descarta certo.</div></html>", 16, Font.PLAIN, new Color(255, 255, 255, 225)), "gapbottom 26");
            for (String linha : new String[]{"O funcionário pesa e registra a entrega",
                    "O sistema calcula e credita os seus pontos", "10.000 pontos viram 10% de desconto no IPTU"}) {
                JLabel l = Tema.rotulo(linha, 15, Font.PLAIN, Color.WHITE);
                l.setIcon(new Bolinha(Tema.VERDE_300, 10));
                l.setIconTextGap(12);
                add(l, "gapbottom 8");
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, Tema.VERDE_900, getWidth(), getHeight(), Tema.VERDE_500));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(255, 255, 255, 20));
            g2.fill(new Ellipse2D.Double(getWidth() - 220, -120, 420, 420));
            g2.fill(new Ellipse2D.Double(-140, getHeight() - 200, 360, 360));
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
