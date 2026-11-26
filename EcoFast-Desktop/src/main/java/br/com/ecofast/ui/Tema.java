package br.com.ecofast.ui;

import br.com.ecofast.model.material.Material;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.UIManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.font.TextAttribute;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Identidade visual do EcoFast: papel, tinta, verde e ouro. É a mesma linguagem da página do projeto:
 * linhas de 2 px em tinta, nenhum canto arredondado, sombra deslocada só no que merece foco,
 * títulos em caixa-alta condensada e números em fonte monoespaçada (leem como recibo de balança).
 *
 * As fontes (Archivo e DM Mono, licença OFL) vêm dentro do próprio aplicativo, em /fontes.
 */
public final class Tema {

    public static final Color PAPEL = new Color(0xF3EEE2);
    public static final Color PAPEL_2 = new Color(0xE9E2D0);
    public static final Color TINTA = new Color(0x12301C);
    public static final Color TINTA_SUAVE = new Color(0x3D5646);
    public static final Color VERDE = new Color(0x1F5E2E);
    public static final Color FOLHA = new Color(0x55A02A);
    public static final Color OURO = new Color(0xE3A21A);
    public static final Color BRANCO = new Color(0xFFFDF7);
    public static final Color ALERTA = new Color(0xA3281C);
    /** Tinta a 35%: o tracejado que separa linhas de tabela e de recibo. */
    public static final Color TRACEJADO = new Color(0x12, 0x30, 0x1C, 90);

    /** A mesma cor de cada material em todas as telas e gráficos (o nome sempre aparece ao lado). */
    private static final Map<String, Color> COR_MATERIAL = Map.of(
            "PAPEL", new Color(0x2F6AA8),
            "PLASTICO", new Color(0xC2410C),
            "VIDRO", new Color(0x1F8A70),
            "METAL", new Color(0x7A7F87),
            "ELETRONICO", new Color(0x7A3FA8));

    private static final Font TEXTO_400 = carregar("Archivo-Regular", Font.SANS_SERIF, Font.PLAIN);
    private static final Font TEXTO_600 = carregar("Archivo-SemiBold", Font.SANS_SERIF, Font.BOLD);
    private static final Font TEXTO_700 = carregar("Archivo-Bold", Font.SANS_SERIF, Font.BOLD);
    private static final Font TEXTO_800 = carregar("Archivo-ExtraBold", Font.SANS_SERIF, Font.BOLD);
    private static final Font COND_800 = carregar("ArchivoCondensed-ExtraBold", Font.SANS_SERIF, Font.BOLD);
    private static final Font COND_900 = carregar("ArchivoCondensed-Black", Font.SANS_SERIF, Font.BOLD);
    private static final Font MONO_400 = carregar("DMMono-Regular", Font.MONOSPACED, Font.PLAIN);
    private static final Font MONO_500 = carregar("DMMono-Medium", Font.MONOSPACED, Font.PLAIN);

    private Tema() {
    }

    private static Font carregar(String nome, String reserva, int estilo) {
        try (InputStream in = Tema.class.getResourceAsStream("/fontes/" + nome + ".ttf")) {
            if (in != null) {
                Font f = Font.createFont(Font.TRUETYPE_FONT, in);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f);
                return f;
            }
        } catch (Exception e) {
            // sem a fonte embutida, cai na fonte padrão do Java
        }
        return new Font(reserva, estilo, 1);
    }

    public static void instalar() {
        // as medidas das telas são em pixels do Java, que já escala sozinho em monitor de 125% ou 150%:
        // sem isto o MigLayout escalaria uma segunda vez
        net.miginfocom.layout.PlatformDefaults.setLogicalPixelBase(net.miginfocom.layout.PlatformDefaults.BASE_REAL_PIXEL);
        FlatLaf.setGlobalExtraDefaults(Map.of(
                "@accentColor", "#1F5E2E",
                "@background", "#F3EEE2",
                "@foreground", "#12301C",
                "@componentBackground", "#F3EEE2"));
        FlatLightLaf.setup();

        UIManager.put("defaultFont", fonte(15, Font.PLAIN));
        for (String chave : List.of("Button.arc", "Component.arc", "TextComponent.arc", "CheckBox.arc", "ProgressBar.arc",
                "Spinner.arc", "ScrollBar.thumbArc", "TabbedPane.tabArc")) {
            UIManager.put(chave, 0);
        }
        UIManager.put("Component.borderWidth", 2);
        UIManager.put("Component.borderColor", TINTA);
        UIManager.put("Component.focusedBorderColor", TINTA);
        UIManager.put("Component.focusColor", OURO);
        UIManager.put("Component.focusWidth", 0);
        UIManager.put("Component.innerFocusWidth", 0);
        for (String campo : List.of("TextField", "PasswordField", "FormattedTextField", "Spinner")) {
            UIManager.put(campo + ".margin", new Insets(10, 12, 10, 12));
            UIManager.put(campo + ".background", PAPEL);
            UIManager.put(campo + ".selectionBackground", OURO);
            UIManager.put(campo + ".selectionForeground", TINTA);
        }
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("ScrollBar.thumb", TINTA_SUAVE);
        UIManager.put("ScrollBar.hoverThumbColor", TINTA);
        UIManager.put("ScrollBar.track", PAPEL_2);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("TabbedPane.showTabSeparators", false);
        UIManager.put("TabbedPane.tabHeight", 46);
        UIManager.put("TabbedPane.font", fonte(15, Font.BOLD));
        UIManager.put("TabbedPane.underlineColor", OURO);
        UIManager.put("TabbedPane.inactiveUnderlineColor", OURO);
        UIManager.put("TabbedPane.underlineHeight", 3);
        UIManager.put("TabbedPane.selectedForeground", TINTA);
        UIManager.put("TabbedPane.hoverColor", PAPEL_2);
        UIManager.put("TabbedPane.focusColor", PAPEL_2);
        UIManager.put("TabbedPane.contentAreaColor", TINTA);
        UIManager.put("TabbedPane.background", PAPEL);
    }

    // ---------- fontes ----------

    /** Aplica tamanho, kerning (o navegador kerna por padrão) e espaço entre letras (em fração do corpo). */
    private static Font ajustar(Font base, float tamanho, float tracking) {
        Map<TextAttribute, Object> a = new HashMap<>();
        a.put(TextAttribute.SIZE, tamanho);
        a.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
        a.put(TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON);
        if (tracking != 0) {
            a.put(TextAttribute.TRACKING, tracking);
        }
        return base.deriveFont(a);
    }

    /** Texto corrido: normal (400) ou negrito (700). */
    public static Font fonte(float tamanho, int estilo) {
        return ajustar((estilo & Font.BOLD) != 0 ? TEXTO_700 : TEXTO_400, tamanho, 0);
    }

    /** Texto com o peso exato do CSS: 400, 600, 700 ou 800. */
    public static Font fontePeso(float tamanho, int peso) {
        Font base = peso >= 800 ? TEXTO_800 : peso >= 700 ? TEXTO_700 : peso >= 600 ? TEXTO_600 : TEXTO_400;
        return ajustar(base, tamanho, 0);
    }

    /** Títulos: Archivo condensada. 800 nos blocos, 900 nos títulos grandes (a partir de 40 px). */
    public static Font fonteTitulo(float tamanho) {
        return fonteTitulo(tamanho, tamanho >= 40 ? 900 : 800);
    }

    public static Font fonteTitulo(float tamanho, int peso) {
        return ajustar(peso >= 900 ? COND_900 : COND_800, tamanho, -0.01f);
    }

    /** Números e rótulos pequenos: largura fixa, alinham em coluna como num recibo. Negrito vira o peso 500. */
    public static Font fonteMono(float tamanho, int estilo) {
        return ajustar((estilo & Font.BOLD) != 0 ? MONO_500 : MONO_400, tamanho, 0);
    }

    /** Mono com espaço entre letras (em fração do corpo), como o letter-spacing do CSS. */
    public static Font fonteMono(float tamanho, int estilo, float tracking) {
        return ajustar((estilo & Font.BOLD) != 0 ? MONO_500 : MONO_400, tamanho, tracking);
    }

    // ---------- textos ----------

    public static Texto rotulo(String texto, float tamanho, int estilo, Color cor) {
        Texto l = new Texto(texto);
        l.setFont(fonte(tamanho, estilo));
        l.setForeground(cor);
        return l;
    }

    public static Texto rotuloPeso(String texto, float tamanho, int peso, Color cor) {
        Texto l = new Texto(texto);
        l.setFont(fontePeso(tamanho, peso));
        l.setForeground(cor);
        return l;
    }

    /** Título em caixa-alta condensada, com a altura de linha apertada (0,98) dos títulos da página. */
    public static Texto titulo(String texto, float tamanho, Color cor) {
        Texto l = new Texto(texto.toUpperCase(Fmt.locale()));
        l.setFont(fonteTitulo(tamanho));
        l.setForeground(cor);
        return l.linha(tamanho * 0.98f);
    }

    public static Texto titulo(String texto, float tamanho, int peso, Color cor) {
        Texto l = new Texto(texto.toUpperCase(Fmt.locale()));
        l.setFont(fonteTitulo(tamanho, peso));
        l.setForeground(cor);
        return l.linha(tamanho * 0.98f);
    }

    /** Número grande em DM Mono peso 500 (saldo, indicadores): leem como o visor de uma balança. */
    public static Texto numero(String texto, float tamanho, Color cor) {
        return mono(texto, tamanho, Font.BOLD, cor);
    }

    public static Texto mono(String texto, float tamanho, int estilo, Color cor) {
        Texto l = new Texto(texto);
        l.setFont(fonteMono(tamanho, estilo));
        l.setForeground(cor);
        return l;
    }

    /** Linha de apoio em caixa-alta espaçada (o "kicker" de cada seção e o nome dos campos). */
    public static Texto kicker(String texto, Color cor) {
        Texto l = new Texto(texto.toUpperCase(Fmt.locale()));
        l.setFont(fonteMono(12, Font.PLAIN, 0.08f));
        l.setForeground(cor);
        return l;
    }

    public static Texto nomeDoCampo(String texto) {
        return kicker(texto, TINTA);
    }

    // ---------- campos ----------

    /** Campo de texto do EcoFast: fundo de papel, linha de 2 px em tinta, letra de 17 px. */
    public static void estilizarCampo(javax.swing.text.JTextComponent campo) {
        campo.setBorder(new br.com.ecofast.ui.componentes.BordaCampo(12, 14));
        campo.setBackground(PAPEL);
        campo.setForeground(TINTA);
        campo.setCaretColor(TINTA);
        campo.setFont(fonte(17, Font.PLAIN));
    }

    // ---------- botões ----------

    public static BotaoEco botaoPrimario(String texto) {
        return new BotaoEco(texto, BotaoEco.Estilo.PRIMARIO, false);
    }

    public static BotaoEco botaoOuro(String texto) {
        return new BotaoEco(texto, BotaoEco.Estilo.OURO, false);
    }

    public static BotaoEco botaoSecundario(String texto) {
        return new BotaoEco(texto, BotaoEco.Estilo.CONTORNO, false);
    }

    public static BotaoEco botaoPequeno(String texto) {
        return new BotaoEco(texto, BotaoEco.Estilo.CONTORNO, true);
    }

    /** Botão que parece um link: só texto com um traço dourado embaixo. */
    public static BotaoEco botaoLink(String texto) {
        return new BotaoEco(texto, BotaoEco.Estilo.LINK, true);
    }

    // ---------- materiais ----------

    public static Color corDoMaterial(Material m) {
        return COR_MATERIAL.getOrDefault(m.getTipo(), FOLHA);
    }

    /** Nome curto para caber nas linhas e legendas: "Papel / Papelão" vira "Papel". */
    public static String nomeCurto(Material m) {
        String nome = m.getNome();
        if (nome.startsWith("Lixo ")) {
            nome = nome.substring(5);
        }
        if (nome.endsWith(" waste")) {
            nome = nome.substring(0, nome.length() - " waste".length());
        }
        int barra = nome.indexOf(" / ");
        if (barra > 0) {
            nome = nome.substring(0, barra);
        }
        return nome.substring(0, 1).toUpperCase() + nome.substring(1);
    }

    // ---------- desenho ----------

    /** Linha tracejada horizontal, a que separa as linhas de tabela e de recibo. */
    public static void tracejado(Graphics2D g, int x1, int x2, int y) {
        g.setColor(TRACEJADO);
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{4f, 3f}, 0f));
        g.drawLine(x1, y, x2, y);
    }
}
