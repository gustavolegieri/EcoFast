package br.com.ecofast.ui;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;

/**
 * Texto de uma linha que se comporta como no CSS: a linha tem uma altura própria ("line-height") e o texto
 * fica centralizado nela pela soma de ascendente e descendente da fonte. Mede o texto pelo desenho real
 * (avanços fracionários), então não perde letras em tela com escala de 125% ou 150%.
 */
public class Texto extends JLabel {

    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);

    private float alturaLinha;

    public Texto(String texto) {
        super(texto);
    }

    /** Altura da linha em pixels (como o line-height do CSS). Zero usa 1,5 vez o tamanho da fonte. */
    public Texto linha(float px) {
        this.alturaLinha = px;
        revalidate();
        return this;
    }

    private float altura() {
        return alturaLinha > 0 ? alturaLinha : getFont().getSize2D() * 1.5f;
    }

    private boolean html() {
        String t = getText();
        return t != null && t.startsWith("<html");
    }

    @Override
    public Dimension getPreferredSize() {
        if (html()) {
            return super.getPreferredSize();
        }
        String t = getText() == null ? "" : getText();
        Insets in = getInsets();
        Icon icone = getIcon();
        double largura = t.isEmpty() ? 0 : getFont().getStringBounds(t, FRC).getWidth();
        int iconeW = icone == null ? 0 : icone.getIconWidth() + (t.isEmpty() ? 0 : getIconTextGap());
        int w = (int) Math.ceil(largura) + iconeW + in.left + in.right;
        int h = Math.round(altura()) + in.top + in.bottom;
        if (icone != null) {
            h = Math.max(h, icone.getIconHeight() + in.top + in.bottom);
        }
        return new Dimension(w, h);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    /** Linha de base do texto, para alinhar títulos e números pela base como o CSS faz ("baseline"). */
    @Override
    public int getBaseline(int largura, int altura) {
        String t = getText() == null ? "" : getText();
        if (html() || t.isEmpty()) {
            return super.getBaseline(largura, altura);
        }
        Insets in = getInsets();
        LineMetrics lm = getFont().getLineMetrics(t, FRC);
        double topo = in.top + (altura - in.top - in.bottom - (lm.getAscent() + lm.getDescent())) / 2.0;
        return (int) Math.round(topo + lm.getAscent());
    }

    @Override
    public BaselineResizeBehavior getBaselineResizeBehavior() {
        return BaselineResizeBehavior.CONSTANT_ASCENT;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        if (html()) {
            super.paintComponent(g0);
            return;
        }
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        if (isOpaque()) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        String t = getText() == null ? "" : getText();
        Insets in = getInsets();
        Icon icone = getIcon();
        Font f = getFont();
        double largura = t.isEmpty() ? 0 : f.getStringBounds(t, FRC).getWidth();
        int iconeW = icone == null ? 0 : icone.getIconWidth() + (t.isEmpty() ? 0 : getIconTextGap());
        double conteudo = largura + iconeW;
        double livre = getWidth() - in.left - in.right;
        double x = in.left;
        int alinhamento = getHorizontalAlignment();
        if (alinhamento == SwingConstants.RIGHT || alinhamento == SwingConstants.TRAILING) {
            x += livre - conteudo;
        } else if (alinhamento == SwingConstants.CENTER) {
            x += (livre - conteudo) / 2;
        }
        if (icone != null) {
            icone.paintIcon(this, g, (int) Math.round(x), in.top + (int) Math.round((getHeight() - in.top - in.bottom - icone.getIconHeight()) / 2.0));
            x += iconeW;
        }
        if (!t.isEmpty()) {
            LineMetrics lm = f.getLineMetrics(t, FRC);
            double topo = in.top + (getHeight() - in.top - in.bottom - (lm.getAscent() + lm.getDescent())) / 2.0;
            g.setFont(f);
            g.setColor(getForeground());
            g.drawString(t, (float) x, (float) (topo + lm.getAscent()));
        }
        g.dispose();
    }
}
