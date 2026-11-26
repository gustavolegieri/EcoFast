package br.com.ecofast.ui;

import br.com.ecofast.infra.BancoSetup;
import br.com.ecofast.infra.ConfigBanco;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.function.BooleanSupplier;

/**
 * Roteiro de teste das telas (não é um teste do JUnit): abre a janela de verdade,
 * clica e digita como uma pessoa faria e salva uma foto de cada tela.
 * Usa um banco separado, EcoFast_QA, que é apagado no fim.
 * Uso: java -cp ... br.com.ecofast.ui.QaTelas pasta-das-fotos
 */
public final class QaTelas {

    private static final String BANCO_QA = "EcoFast_QA";
    private static Path saida;

    public static void main(String[] args) throws Exception {
        saida = Path.of(args[0]);
        Files.createDirectories(saida);
        // Sem config salva, vale o que vier nas variáveis ECOFAST_QA_* (a senha nunca vai na linha de comando).
        ConfigBanco base = ConfigBanco.carregar().orElseGet(() -> {
            String senha = System.getenv("ECOFAST_QA_SENHA");
            if (senha == null) {
                throw new IllegalStateException("sem config salva e sem ECOFAST_QA_SENHA");
            }
            return new ConfigBanco(System.getenv().getOrDefault("ECOFAST_QA_SERVIDOR", "localhost"),
                    Integer.parseInt(System.getenv().getOrDefault("ECOFAST_QA_PORTA", "1433")),
                    ConfigBanco.BANCO_PADRAO, System.getenv().getOrDefault("ECOFAST_QA_USUARIO", "sa"), senha);
        });
        ConfigBanco qa = base.comBanco(BANCO_QA);
        apagar(qa);
        BancoSetup.garantir(qa);

        Tema.instalar();
        JanelaPrincipal janela = new JanelaPrincipal(qa);
        naEdt(janela::iniciar);
        esperar("login liberado", () -> achar(janela, "botaoEntrar", AbstractButton.class).isEnabled(), 15000);
        Thread.sleep(400);
        foto(janela, "q01-login");

        // ---- cidadã ----
        clicar(janela, "alternadorDemo");
        clicar(janela, "demoCidada");
        foto(janela, "q02-login-preenchido");
        clicar(janela, "botaoEntrar");
        esperar("painel do cidadão", () -> achar(janela, "quemEstaLogado", JLabel.class).getText().contains("Maria"), 8000);
        esperar("extrato carregado", () -> existeTexto(janela, "Faltam"), 8000);
        Thread.sleep(300);
        foto(janela, "q03-cidadao");
        fotoCompleta(janela, "f03-cidadao");
        clicar(janela, "botaoSair");

        // ---- senha errada ----
        naEdt(() -> {
            achar(janela, "campoEmail", javax.swing.JTextField.class).setText("maria@email.com");
            achar(janela, "campoSenha", javax.swing.JPasswordField.class).setText("errada");
        });
        clicar(janela, "botaoEntrar");
        esperar("erro de login", () -> achar(janela, "erroLogin", JLabel.class).isVisible(), 8000);
        foto(janela, "q04-login-erro");

        // ---- funcionário ----
        clicar(janela, "demoFuncionario");
        clicar(janela, "botaoEntrar");
        esperar("painel do funcionário", () -> achar(janela, "quemEstaLogado", JLabel.class).getText().contains("Pedro"), 8000);
        esperar("lista de registros", () -> tabelaComLinhas(janela), 8000);
        Thread.sleep(300);
        foto(janela, "q05-funcionario");
        naEdt(() -> achar(janela, "cpf", JFormattedTextField.class).setText("111.111.111-11"));
        clicar(janela, "buscar");
        esperar("cidadão encontrado", () -> existeTexto(janela, "saldo atual"), 8000);
        clicar(janela, "material-ELETRONICO");
        naEdt(() -> achar(janela, "peso", JSpinner.class).setValue(2.5));
        Thread.sleep(200);
        foto(janela, "q06-formulario");
        fotoCompleta(janela, "f06-formulario");
        clicar(janela, "salvar");
        esperar("recibo", () -> existeTexto(janela, "registrado com sucesso"), 8000);
        Thread.sleep(600);
        foto(janela, "q07-recibo");
        naEdt(() -> achar(janela, "cpf", JFormattedTextField.class).setText("999.999.999-99"));
        clicar(janela, "salvar");
        esperar("erro de CPF", () -> existeTexto(janela, "Nenhum cidadão cadastrado"), 8000);
        foto(janela, "q08-erro-cpf");
        clicar(janela, "botaoSair");

        // ---- administrador ----
        clicar(janela, "demoAdmin");
        clicar(janela, "botaoEntrar");
        esperar("painel do admin", () -> achar(janela, "quemEstaLogado", JLabel.class).getText().contains("Admin"), 8000);
        esperar("relatório carregado", () -> existeTexto(janela, "últimos 30 dias"), 8000);
        Thread.sleep(1500);
        foto(janela, "q09-admin");
        fotoCompleta(janela, "f09-admin");
        Component barras = acharPorClasse(janela, "GraficoBarras");
        int x = barras.getWidth() - 120, y = barras.getHeight() - 70;
        naEdt(() -> barras.dispatchEvent(new MouseEvent(barras, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, x, y, 0, false)));
        Thread.sleep(250);
        foto(janela, "q10-admin-hover-barra");
        Component materiais = acharPorClasse(janela, "GraficoMateriais");
        int mx = materiais.getWidth() / 2, my = 20;
        naEdt(() -> materiais.dispatchEvent(new MouseEvent(materiais, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, mx, my, 0, false)));
        Thread.sleep(250);
        foto(janela, "q11-admin-hover-material");
        naEdt(() -> achar(janela, "abas", JTabbedPane.class).setSelectedIndex(1));
        Thread.sleep(900);
        foto(janela, "q12-admin-registro");
        fotoCompleta(janela, "f12-admin-registro");
        naEdt(() -> achar(janela, "abas", JTabbedPane.class).setSelectedIndex(0));
        Thread.sleep(900);
        clicar(janela, "botaoSair");

        // ---- tela de conexão ----
        naEdt(() -> {
            DialogoConexao d = new DialogoConexao(janela, qa, "Teste de aviso: não consegui conectar ao servidor.");
            try {
                foto(d.getContentPane(), "q13-dialogo-conexao");
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });

        naEdt(janela::dispose);
        apagar(qa);
        System.out.println("QA concluído: fotos em " + saida);
        System.exit(0);
    }

    // ---------- ajudantes ----------

    private static void naEdt(Runnable r) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeAndWait(r);
        }
    }

    private static void esperar(String o_que, BooleanSupplier condicao, long limiteMs) throws Exception {
        long fim = System.currentTimeMillis() + limiteMs;
        while (System.currentTimeMillis() < fim) {
            boolean[] ok = {false};
            naEdt(() -> {
                try {
                    ok[0] = condicao.getAsBoolean();
                } catch (RuntimeException e) {
                    ok[0] = false;
                }
            });
            if (ok[0]) {
                return;
            }
            Thread.sleep(60);
        }
        throw new IllegalStateException("Tempo esgotado esperando: " + o_que);
    }

    private static <T extends Component> T achar(Container raiz, String nome, Class<T> tipo) {
        Component c = buscar(raiz, nome);
        if (c == null) {
            throw new IllegalStateException("Componente não encontrado: " + nome);
        }
        return tipo.cast(c);
    }

    private static Component buscar(Component c, String nome) {
        if (nome.equals(c.getName())) {
            return c;
        }
        if (c instanceof Container cont) {
            for (Component filho : cont.getComponents()) {
                Component r = buscar(filho, nome);
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    private static Component acharPorClasse(Component c, String nomeSimples) {
        if (c.getClass().getSimpleName().equals(nomeSimples) && c.isShowing()) {
            return c;
        }
        if (c instanceof Container cont) {
            for (Component filho : cont.getComponents()) {
                Component r = acharPorClasse(filho, nomeSimples);
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    private static boolean tabelaComLinhas(Component c) {
        if (c instanceof br.com.ecofast.ui.componentes.TabelaDados t && t.isShowing() && t.quantidadeDeLinhas() > 0) {
            return true;
        }
        if (c instanceof Container cont) {
            for (Component filho : cont.getComponents()) {
                if (tabelaComLinhas(filho)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean existeTexto(Component c, String trecho) {
        if (c instanceof JLabel l && c.isShowing() && l.getText() != null && l.getText().contains(trecho)) {
            return true;
        }
        if (c instanceof Container cont) {
            for (Component filho : cont.getComponents()) {
                if (existeTexto(filho, trecho)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void clicar(Container raiz, String nome) throws Exception {
        naEdt(() -> achar(raiz, nome, AbstractButton.class).doClick(60));
        Thread.sleep(120);
    }

    /** Imprime a largura da tabela, da área visível e das colunas (para conferir se preenchem o cartão). */
    private static void relatarTabelas(Component c, String onde) {
        if (c instanceof br.com.ecofast.ui.componentes.TabelaDados t && t.isShowing()) {
            var tabela = (javax.swing.JTable) t.getViewport().getView();
            int soma = 0;
            for (int i = 0; i < tabela.getColumnCount(); i++) {
                soma += tabela.getColumnModel().getColumn(i).getWidth();
            }
            Container pai = t.getParent();
            System.out.printf("[%s] tabela: x=%d painel=%d area=%d soma-colunas=%d | pai=%s largura=%d insets=%s layout=%s%n",
                    onde, t.getX(), t.getWidth(), t.getViewport().getWidth(), soma, pai.getClass().getSimpleName(),
                    pai.getWidth(), pai.getInsets(), pai.getLayout().getClass().getSimpleName());
            pai.invalidate();
            pai.validate();
            System.out.printf("      depois de forcar o layout do cartao: tabela largura=%d (cartao=%d)%n", t.getWidth(), pai.getWidth());
            Container avo = pai.getParent();
            System.out.printf("      avo=%s largura=%d layout=%s%n", avo.getClass().getSimpleName(), avo.getWidth(), avo.getLayout().getClass().getSimpleName());
        }
        if (c instanceof Container cont) {
            for (Component filho : cont.getComponents()) {
                relatarTabelas(filho, onde);
            }
        }
    }

    private static void foto(Component c, String nome) throws Exception {
        naEdt(() -> relatarTabelas(c, nome));
        naEdt(() -> {
            try {
                if (c.getWidth() <= 0) {
                    c.setSize(c.getPreferredSize());
                    ((Container) c).doLayout();
                }
                BufferedImage img = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g = img.createGraphics();
                g.setColor(java.awt.Color.WHITE);
                g.fillRect(0, 0, img.getWidth(), img.getHeight());
                c.paint(g);
                g.dispose();
                ImageIO.write(img, "png", saida.resolve(nome + ".png").toFile());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
        System.out.println("foto " + nome);
    }

    /** Foto da tela inteira, inclusive o que fica abaixo da dobra (a janela só mostra o pedaço visível). */
    private static void fotoCompleta(Container janela, String nome) throws Exception {
        naEdt(() -> {
            try {
                Container c = (Container) acharPorClasse(janela, "ConteudoRolavel");
                java.awt.Dimension original = c.getSize();
                int largura = original.width;
                c.setSize(largura, 4000);
                c.validate();
                int altura = c.getPreferredSize().height;
                c.setSize(largura, altura);
                c.validate();
                BufferedImage img = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = img.createGraphics();
                g.setColor(java.awt.Color.WHITE);
                g.fillRect(0, 0, largura, altura);
                c.paint(g);
                g.dispose();
                ImageIO.write(img, "png", saida.resolve(nome + ".png").toFile());
                c.setSize(original);
                c.validate();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
        System.out.println("foto " + nome);
    }

    private static void apagar(ConfigBanco cfg) throws Exception {
        try (Connection c = DriverManager.getConnection(cfg.comBanco("master").url(), cfg.usuario(), cfg.senha());
             Statement st = c.createStatement()) {
            st.execute("IF DB_ID(N'" + BANCO_QA + "') IS NOT NULL BEGIN ALTER DATABASE [" + BANCO_QA
                    + "] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; DROP DATABASE [" + BANCO_QA + "]; END");
        }
    }
}
