package br.com.ecofast.ui;

import br.com.ecofast.model.material.Material;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;
import java.util.Map;

/** Cores, fontes e o jeito de montar botões e rótulos, para todas as telas parecerem uma só. */
public final class Tema {

    public static final Color VERDE_900 = new Color(0x1B5E20);
    public static final Color VERDE_700 = new Color(0x2E7D32);
    public static final Color VERDE_500 = new Color(0x43A047);
    public static final Color VERDE_300 = new Color(0xA5D6A7);
    public static final Color VERDE_100 = new Color(0xE8F5E9);
    public static final Color VERDE_50 = new Color(0xF3FAF3);
    public static final Color FUNDO = new Color(0xF5F8F5);
    public static final Color BORDA = new Color(0xDFE7DF);
    public static final Color TEXTO = new Color(0x1F2A1F);
    public static final Color SUAVE = new Color(0x5B6B5B);
    public static final Color ERRO = new Color(0xC62828);
    public static final Color ERRO_FUNDO = new Color(0xFDECEA);
    public static final Color MOEDA = new Color(0xB26A00);
    public static final Color MOEDA_COR = new Color(0xF9A825);

    /** A mesma cor de cada material em todas as telas e gráficos. */
    private static final Map<String, Color> COR_MATERIAL = Map.of(
            "PAPEL", new Color(0x1E88E5),
            "PLASTICO", new Color(0xE53935),
            "VIDRO", new Color(0x43A047),
            "METAL", new Color(0xFB8C00),
            "ELETRONICO", new Color(0x8E24AA));

    private Tema() {
    }

    public static void instalar() {
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));
        FlatLaf.setGlobalExtraDefaults(Map.of("@accentColor", "#2E7D32"));
        FlatLightLaf.setup();
        UIManager.put("Button.arc", 14);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("ProgressBar.arc", 999);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("TextField.margin", new Insets(6, 10, 6, 10));
        UIManager.put("PasswordField.margin", new Insets(6, 10, 6, 10));
        UIManager.put("FormattedTextField.margin", new Insets(6, 10, 6, 10));
        UIManager.put("Spinner.arc", 12);
        UIManager.put("TabbedPane.showTabSeparators", false);
        UIManager.put("TabbedPane.tabHeight", 44);
        UIManager.put("TabbedPane.font", fonte(14, Font.BOLD));
    }

    public static Font fonte(float tamanho, int estilo) {
        Font base = UIManager.getFont("Label.font");
        if (base == null) {
            base = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
        }
        return base.deriveFont(estilo, tamanho);
    }

    public static JLabel rotulo(String texto, float tamanho, int estilo, Color cor) {
        JLabel l = new JLabel(texto);
        l.setFont(fonte(tamanho, estilo));
        l.setForeground(cor);
        return l;
    }

    public static JButton botaoPrimario(String texto) {
        JButton b = new JButton(texto);
        b.putClientProperty(FlatClientProperties.STYLE,
                "background: #2E7D32; foreground: #FFFFFF; hoverBackground: #256A29; pressedBackground: #1B5E20;"
                        + "focusedBackground: #2E7D32; borderWidth: 0; focusWidth: 0; arc: 14; margin: 10,22,10,22");
        b.setFont(fonte(14, Font.BOLD));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton botaoSecundario(String texto) {
        JButton b = new JButton(texto);
        b.putClientProperty(FlatClientProperties.STYLE,
                "background: #E8F5E9; foreground: #1B5E20; hoverBackground: #D4ECD5; pressedBackground: #C2E4C4;"
                        + "focusedBackground: #E8F5E9; borderWidth: 0; focusWidth: 0; arc: 14; margin: 9,18,9,18");
        b.setFont(fonte(14, Font.BOLD));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Botão que parece um link: sem fundo, só texto. */
    public static JButton botaoLink(String texto) {
        JButton b = new JButton(texto);
        b.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);
        b.setForeground(VERDE_700);
        b.setFont(fonte(13, Font.BOLD));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static Color corDoMaterial(Material m) {
        return COR_MATERIAL.getOrDefault(m.getTipo(), VERDE_500);
    }

    /** Nome curto para caber nos botões: "Papel / Papelão" vira "Papel". */
    public static String nomeCurto(Material m) {
        String nome = m.getNome();
        if (nome.startsWith("Lixo ")) {
            nome = nome.substring(5);
        }
        int barra = nome.indexOf(" / ");
        if (barra > 0) {
            nome = nome.substring(0, barra);
        }
        return nome.substring(0, 1).toUpperCase() + nome.substring(1);
    }
}
