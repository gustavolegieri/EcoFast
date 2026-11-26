package br.com.ecofast.ui;

import br.com.ecofast.model.Regras;
import br.com.ecofast.ui.componentes.Cartao;
import br.com.ecofast.ui.componentes.Marcador;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Path2D;

/** Primeira tela: à esquerda a marca e a promessa do app, à direita o formulário de entrada. Medidas da página. */
public final class TelaLogin extends JPanel {

    /** O que a tela precisa que a janela faça por ela. */
    public interface Acoes {
        void entrar(String email, String senha);

        void configurarConexao();
    }

    public enum Estado { CONECTANDO, OK, ERRO }

    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);

    private final JTextField campoEmail = new JTextField();
    private final JPasswordField campoSenha = new JPasswordField();
    private final JButton botaoEntrar = Tema.botaoPrimario("Entrar");
    private final Texto erro = Tema.rotulo(" ", 14, Font.BOLD, Tema.ALERTA);
    private final Texto status = Tema.mono(" ", 12.5f, Font.PLAIN, Tema.TINTA);

    public TelaLogin(Acoes acoes) {
        super(new GridLayout(1, 2));
        add(new PainelMarca());
        add(criarLadoDoFormulario(acoes));
    }

    private JPanel criarLadoDoFormulario(Acoes acoes) {
        JPanel lado = new JPanel(new MigLayout("insets 56 64 56 64, fill", "[grow, center]", "[grow, center]"));
        lado.setBackground(Tema.PAPEL);

        // a sombra de 8 px do cartão não ocupa espaço na página: aqui ela é descontada dos espaços vizinhos
        JPanel coluna = new JPanel(new MigLayout("insets 0, gap 0, wrap 1, hidemode 3", "[468!]", "[]28[]"));
        coluna.setOpaque(false);

        Cartao cartao = new Cartao(new MigLayout("insets 0, gap 0, wrap 1, hidemode 3", "[396!]", ""))
                .margens(34, 34, 34, 30).sombra(8, Tema.TINTA);
        cartao.add(Tema.kicker("Acesso", Tema.VERDE), "h 18!");
        cartao.add(Tema.titulo("Entrar", 40, 900, Tema.TINTA).linha(39), "gaptop 10, h 39!");
        cartao.add(Tema.rotulo("Use o e-mail e a senha do seu perfil.", 15, Font.PLAIN, Tema.TINTA_SUAVE), "gaptop 10, h 23!");

        cartao.add(Tema.nomeDoCampo("E-mail"), "gaptop 18, h 18!");
        campoEmail.setName("campoEmail");
        Tema.estilizarCampo(campoEmail);
        campoEmail.putClientProperty("JTextField.placeholderText", "voce@email.com");
        cartao.add(campoEmail, "gaptop 6, w 392!, h 53!");

        cartao.add(Tema.nomeDoCampo("Senha"), "gaptop 18, h 18!");
        campoSenha.setName("campoSenha");
        Tema.estilizarCampo(campoSenha);
        cartao.add(campoSenha, "gaptop 6, w 392!, h 53!");

        erro.setName("erroLogin");
        erro.setVisible(false);
        cartao.add(erro, "gaptop 10");

        botaoEntrar.setName("botaoEntrar");
        botaoEntrar.setEnabled(false);
        botaoEntrar.addActionListener(e -> acoes.entrar(campoEmail.getText(), new String(campoSenha.getPassword())));
        campoEmail.addActionListener(e -> campoSenha.requestFocusInWindow());
        campoSenha.addActionListener(e -> botaoEntrar.doClick());
        cartao.add(botaoEntrar, "gaptop 24, w 396!, h 53!");
        coluna.add(cartao);

        // as contas de demonstração ficam guardadas: quem precisa abre, quem não precisa nem vê
        JPanel rodape = new JPanel(new MigLayout("insets 0, gap 0, wrap 1, fillx, hidemode 3", "[grow, fill]", ""));
        rodape.setOpaque(false);
        JPanel demos = new JPanel(new MigLayout("insets 0, gap 8 0, hidemode 3", "[][][][]", "[]"));
        demos.setOpaque(false);
        demos.setVisible(false);
        demos.add(demo("Cidadã", "maria@email.com", "demoCidada"));
        demos.add(demo("Funcionário", "funcionario@ecofast.com", "demoFuncionario"));
        demos.add(demo("Administrador", "admin@ecofast.com", "demoAdmin"));
        demos.add(Tema.mono("senha 123456", 12, Font.PLAIN, Tema.TINTA_SUAVE), "gapleft 4");
        Alternador alternador = new Alternador("Contas de demonstração");
        alternador.setName("alternadorDemo");
        alternador.addActionListener(e -> {
            demos.setVisible(alternador.isSelected());
            coluna.revalidate();
            coluna.repaint();
        });
        rodape.add(alternador, "alignx left, h 19!");
        rodape.add(demos, "gaptop 12");

        status.setName("statusConexao");
        JButton conexao = Tema.botaoLink("Conexão com o banco").tamanho(13);
        conexao.setName("botaoConexao");
        conexao.addActionListener(e -> acoes.configurarConexao());
        JPanel linhaStatus = new JPanel(new MigLayout("insets 14 0 0 0, gap 0, fillx", "[grow][]", "[]")) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Tema.tracejado(g2, 0, getWidth(), 1);
                g2.dispose();
            }
        };
        linhaStatus.setOpaque(false);
        linhaStatus.add(status, "aligny center");
        linhaStatus.add(conexao, "aligny center");
        rodape.add(linhaStatus, "gaptop 14, growx");
        coluna.add(rodape, "w 460!");

        lado.add(coluna, "gapleft 8");
        return lado;
    }

    private JButton demo(String texto, String email, String nome) {
        Pilula b = new Pilula(texto);
        b.setName(nome);
        b.addActionListener(e -> {
            campoEmail.setText(email);
            campoSenha.setText("123456");
            campoSenha.requestFocusInWindow();
        });
        return b;
    }

    public void mostrarErro(String texto) {
        erro.setText(texto == null ? " " : texto);
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
            case CONECTANDO -> Tema.OURO;
            case OK -> Tema.FOLHA;
            case ERRO -> Tema.ALERTA;
        };
        status.setIcon(new Marcador(cor, 10));
        status.setIconTextGap(8);
        status.setText(texto);
    }

    /** Botãozinho com borda fina: escolhe uma das contas de demonstração. */
    private static final class Pilula extends JButton {
        Pilula(String texto) {
            super(texto);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(Tema.fontePeso(13, 600));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension((int) Math.ceil(getFont().getStringBounds(getText(), FRC).getWidth()) + 27, 33);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            if (getModel().isRollover()) {
                g.setColor(Tema.PAPEL_2);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            g.setColor(Tema.TINTA);
            g.setStroke(new BasicStroke(1.5f));
            g.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
            g.setFont(getFont());
            LineMetrics lm = getFont().getLineMetrics(getText(), FRC);
            double largura = getFont().getStringBounds(getText(), FRC).getWidth();
            g.drawString(getText(), (float) ((getWidth() - largura) / 2), (float) ((getHeight() - (lm.getAscent() + lm.getDescent())) / 2 + lm.getAscent()));
            if (isFocusOwner()) {
                g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{3f, 3f}, 0f));
                g.drawRect(5, 5, getWidth() - 11, getHeight() - 11);
            }
            g.dispose();
        }
    }

    /** Texto pequeno com uma setinha que gira ao abrir: mostra e esconde um bloco sem ocupar espaço. */
    private static final class Alternador extends JToggleButton {

        Alternador(String texto) {
            super(texto);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(Tema.fonteMono(12.5f, Font.PLAIN));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension((int) Math.ceil(getFont().getStringBounds(getText(), FRC).getWidth()) + 18, 19);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            Color cor = getModel().isRollover() ? Tema.TINTA : Tema.TINTA_SUAVE;
            g.setColor(cor);
            Graphics2D seta = (Graphics2D) g.create();
            seta.translate(5, getHeight() / 2.0);
            if (isSelected()) {
                seta.rotate(Math.PI / 2);
            }
            Path2D p = new Path2D.Double();
            p.moveTo(-2, -4);
            p.lineTo(2, 0);
            p.lineTo(-2, 4);
            seta.setStroke(new BasicStroke(1.6f));
            seta.draw(p);
            seta.dispose();
            g.setFont(getFont());
            LineMetrics lm = getFont().getLineMetrics(getText(), FRC);
            g.drawString(getText(), 18f, (float) ((getHeight() - (lm.getAscent() + lm.getDescent())) / 2 + lm.getAscent()));
            if (isFocusOwner()) {
                g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{3f, 3f}, 0f));
                g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            }
            g.dispose();
        }
    }

    /** Lado esquerdo: fundo verde chapado com a promessa do app. O título cresce com a largura da janela. */
    private static final class PainelMarca extends JPanel {

        private final Texto linha1 = new Texto("LIXO QUE");
        private final Texto linha2 = new Texto("ABATE");
        private final Texto linha3 = new Texto("O IPTU");
        private float corpo = 102.4f;

        PainelMarca() {
            super(new MigLayout("insets 56 64 56 64, gap 0, wrap 1, fillx", "[grow, fill]", "[]0[grow, bottom]"));
            setOpaque(false);

            JPanel marca = new JPanel(new MigLayout("insets 0, gap 10 0", "[40!][]", "[40!]"));
            marca.setOpaque(false);
            JLabel logo = new JLabel(Icone.vetorial(40, true));
            marca.add(logo);
            marca.add(Tema.titulo("EcoFast", 30, 900, Tema.PAPEL).linha(30), "aligny center");
            add(marca);

            JPanel bloco = new JPanel(new MigLayout("insets 0, gap 0, wrap 1, fillx", "[grow, fill]", ""));
            bloco.setOpaque(false);
            bloco.add(Tema.kicker("Gestão de ecopontos", Tema.OURO), "h 18!, gapbottom 16");
            for (Texto t : new Texto[]{linha1, linha2, linha3}) {
                t.setForeground(t == linha2 ? Tema.TINTA : Tema.PAPEL);
                bloco.add(t, "growx");
            }
            aplicarCorpo();

            JPanel lista = new JPanel(new MigLayout("insets 0, gap 0, wrap 1, fillx", "[grow, fill]", ""));
            lista.setOpaque(false);
            String[] passos = {"O funcionário pesa e registra a entrega.", "O sistema calcula e credita os seus pontos.",
                    "A cada " + Fmt.inteiro(Regras.PONTOS_POR_FAIXA) + " pontos, "
                            + Regras.DESCONTO_IPTU_PERCENTUAL + "% de desconto no IPTU."};
            for (int i = 0; i < passos.length; i++) {
                lista.add(new Passo(String.format("%02d", i + 1), passos[i], i == 0), "growx");
            }
            bloco.add(lista, "gaptop 56, growx, wmax 480");
            add(bloco, "growx");

            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    // 8vw da página inteira, entre 60 e 112 px (a página tem o dobro desta metade)
                    float novo = Math.max(60f, Math.min(112f, getWidth() * 2 * 0.08f));
                    if (Math.abs(novo - corpo) > 0.5f) {
                        corpo = novo;
                        aplicarCorpo();
                    }
                }
            });
        }

        private void aplicarCorpo() {
            Font f = Tema.fonteTitulo(corpo, 900);
            for (Texto t : new Texto[]{linha1, linha2, linha3}) {
                t.setFont(f);
                t.linha(Math.round(corpo * 0.9f));
            }
            linha2.setBorder(new EmptyBorder(0, Math.round(corpo * 0.06f), 0, 0));
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setColor(Tema.VERDE);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Tema.TINTA);
            g.fillRect(getWidth() - 2, 0, 2, getHeight());
            // o realce dourado de "abate" cobre a área da fonte (maior que a linha), com um respiro de 0,12 em
            if (linha2.getParent() != null) {
                Point p = SwingUtilities.convertPoint(linha2.getParent(), linha2.getLocation(), this);
                LineMetrics lm = linha2.getFont().getLineMetrics("ABATE", FRC);
                double texto = linha2.getFont().getStringBounds("ABATE", FRC).getWidth();
                double altura = lm.getAscent() + lm.getDescent();
                double x = p.x - corpo * 0.06;
                double largura = corpo * 0.06 + texto + corpo * 0.12 + corpo * 0.06;
                double y = p.y + (linha2.getHeight() - altura) / 2.0;
                g.setColor(Tema.OURO);
                g.fill(new java.awt.geom.Rectangle2D.Double(x, y, largura, altura));
            }
            g.dispose();
        }
    }

    /** Uma linha numerada da lista de promessas, separada da seguinte por um fio claro de 1 px. */
    private static final class Passo extends JPanel {
        private final boolean primeiro;

        Passo(String numero, String texto, boolean primeiro) {
            super(new MigLayout("insets " + (primeiro ? 17 : 16) + " 0 17 0, gap 18 0", "[28!][grow]", "[27!]"));
            this.primeiro = primeiro;
            setOpaque(false);
            add(Tema.mono(numero, 18, Font.PLAIN, Tema.OURO).linha(27));
            add(Tema.rotulo(texto, 18, Font.PLAIN, Tema.PAPEL).linha(27));
        }

        @Override
        protected void paintBorder(Graphics g) {
            g.setColor(new Color(0xF3, 0xEE, 0xE2, 115));
            if (primeiro) {
                g.fillRect(0, 0, getWidth(), 1);
            }
            g.fillRect(0, getHeight() - 1, getWidth(), 1);
        }
    }
}
