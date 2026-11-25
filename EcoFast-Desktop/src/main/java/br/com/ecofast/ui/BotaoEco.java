package br.com.ecofast.ui;

import javax.swing.JButton;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;

/**
 * Botão do EcoFast, com as medidas do botão da página: borda de 2 px, entrelinha 1,2 e margem interna de
 * 13 x 22 px (7 x 14 no pequeno). O primário e o dourado têm sombra deslocada de 4 px, que "afunda" ao passar
 * o mouse e ao clicar; o de contorno é só a linha; o link é texto com um traço dourado embaixo.
 * A sombra faz parte da área do componente (4 px à direita e embaixo), então quem posiciona o botão
 * reserva esse espaço.
 */
public final class BotaoEco extends JButton {

    public enum Estilo { PRIMARIO, OURO, CONTORNO, LINK }

    /** Espaço que a sombra ocupa à direita e embaixo do botão. */
    public static final int SOMBRA = 4;

    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);

    private final Estilo estilo;
    private final boolean pequeno;
    private float tamanhoLink = 14;

    BotaoEco(String texto, Estilo estilo, boolean pequeno) {
        super(texto);
        this.estilo = estilo;
        this.pequeno = pequeno;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        atualizarFonte();
    }

    /** Tamanho da letra do botão link (o padrão é 14 px). */
    public BotaoEco tamanho(float px) {
        this.tamanhoLink = px;
        atualizarFonte();
        return this;
    }

    private void atualizarFonte() {
        float t = estilo == Estilo.LINK ? tamanhoLink : pequeno ? 14 : 16;
        setFont(Tema.fonte(t, Font.BOLD));
        revalidate();
    }

    private boolean temSombra() {
        return estilo == Estilo.PRIMARIO || estilo == Estilo.OURO;
    }

    private double larguraDoTexto() {
        return getText() == null || getText().isEmpty() ? 0 : getFont().getStringBounds(getText(), FRC).getWidth();
    }

    @Override
    public Dimension getPreferredSize() {
        double texto = larguraDoTexto();
        float t = getFont().getSize2D();
        if (estilo == Estilo.LINK) {
            return new Dimension((int) Math.ceil(texto), (int) Math.ceil(t * 1.5f) + 3);
        }
        int w = (int) Math.ceil(texto) + (pequeno ? 28 : 44) + 4 + (temSombra() ? SOMBRA : 0);
        int h = (int) Math.ceil(t * 1.2f) + (pequeno ? 14 : 26) + 4 + (temSombra() ? SOMBRA : 0);
        return new Dimension(w, h);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    /** Linha de base do texto (só do botão link), para alinhar pela base com o título ao lado. */
    @Override
    public int getBaseline(int largura, int altura) {
        if (estilo != Estilo.LINK) {
            return super.getBaseline(largura, altura);
        }
        LineMetrics lm = getFont().getLineMetrics(getText() == null ? "" : getText(), FRC);
        double topo = (getFont().getSize2D() * 1.5f - (lm.getAscent() + lm.getDescent())) / 2.0;
        return (int) Math.round(topo + lm.getAscent());
    }

    @Override
    public BaselineResizeBehavior getBaselineResizeBehavior() {
        return BaselineResizeBehavior.CONSTANT_ASCENT;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        if (!isEnabled()) {
            g.setComposite(AlphaComposite.SrcOver.derive(0.45f));
        }
        g.setFont(getFont());
        String texto = getText() == null ? "" : getText();
        LineMetrics lm = getFont().getLineMetrics(texto, FRC);
        double largura = larguraDoTexto();

        if (estilo == Estilo.LINK) {
            float t = getFont().getSize2D();
            double topo = (t * 1.5f - (lm.getAscent() + lm.getDescent())) / 2.0;
            g.setColor(Tema.TINTA);
            g.drawString(texto, 0f, (float) (topo + lm.getAscent()));
            g.setColor(getModel().isRollover() ? Tema.TINTA : Tema.OURO);
            g.fillRect(0, (int) Math.round(t * 1.5f) + 1, (int) Math.ceil(largura), 2);
            pintarFoco(g, 0, 0, getWidth(), getHeight(), Tema.TINTA);
            g.dispose();
            return;
        }

        int s = temSombra() ? SOMBRA : 0;
        int w = getWidth() - s, h = getHeight() - s;
        boolean afundado = temSombra() && (getModel().isRollover() || getModel().isPressed()) && isEnabled();
        int d = afundado ? 2 : 0;
        int deslocamento = afundado ? 2 : s;

        Color fundo = switch (estilo) {
            case PRIMARIO -> Tema.TINTA;
            case OURO -> Tema.OURO;
            default -> getModel().isRollover() && isEnabled() ? Tema.PAPEL_2 : new Color(0, 0, 0, 0);
        };
        Color corTexto = estilo == Estilo.PRIMARIO ? Tema.PAPEL : Tema.TINTA;

        if (temSombra()) {
            g.setColor(estilo == Estilo.PRIMARIO ? Tema.OURO : Tema.TINTA);
            g.fillRect(d + deslocamento, d + deslocamento, w, h);
        }
        g.setColor(fundo);
        g.fillRect(d, d, w, h);
        g.setColor(Tema.TINTA);
        g.setStroke(new BasicStroke(2f));
        g.drawRect(d + 1, d + 1, w - 2, h - 2);

        g.setColor(corTexto);
        double x = d + (w - largura) / 2.0;
        double topo = d + (h - (lm.getAscent() + lm.getDescent())) / 2.0;
        g.drawString(texto, (float) x, (float) (topo + lm.getAscent()));
        pintarFoco(g, d + 5, d + 5, w - 10, h - 10, estilo == Estilo.PRIMARIO ? Tema.OURO : Tema.TINTA);
        g.dispose();
    }

    /** Quem navega pelo teclado precisa ver onde está: um contorno tracejado por dentro do botão. */
    private void pintarFoco(Graphics2D g, int x, int y, int w, int h, Color cor) {
        if (!isFocusOwner()) {
            return;
        }
        g.setColor(cor);
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{3f, 3f}, 0f));
        g.drawRect(x, y, w - 1, h - 1);
    }
}
