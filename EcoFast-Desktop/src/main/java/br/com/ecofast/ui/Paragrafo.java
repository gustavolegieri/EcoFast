package br.com.ecofast.ui;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;

/** Parágrafo com quebra de linha, largura máxima fixa e altura de linha própria, como um bloco de texto do CSS. */
public class Paragrafo extends JComponent {

    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);

    private final String texto;
    private final int larguraMax;
    private final float alturaLinha;
    private List<TextLayout> linhas;

    public Paragrafo(String texto, Font fonte, Color cor, float alturaLinha, int larguraMax) {
        this.texto = texto;
        this.larguraMax = larguraMax;
        this.alturaLinha = alturaLinha;
        setFont(fonte);
        setForeground(cor);
        setOpaque(false);
    }

    private List<TextLayout> quebrar(int largura) {
        List<TextLayout> saida = new ArrayList<>();
        if (texto.isEmpty()) {
            return saida;
        }
        AttributedString as = new AttributedString(texto);
        as.addAttribute(TextAttribute.FONT, getFont());
        LineBreakMeasurer m = new LineBreakMeasurer(as.getIterator(), FRC);
        while (m.getPosition() < texto.length()) {
            saida.add(m.nextLayout(largura));
        }
        return saida;
    }

    @Override
    public Dimension getPreferredSize() {
        List<TextLayout> l = quebrar(larguraMax);
        double maior = 0;
        for (TextLayout t : l) {
            maior = Math.max(maior, t.getAdvance());
        }
        return new Dimension((int) Math.ceil(maior) + 1, (int) Math.ceil(l.size() * alturaLinha));
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setColor(getForeground());
        List<TextLayout> l = quebrar(larguraMax);
        for (int i = 0; i < l.size(); i++) {
            TextLayout t = l.get(i);
            float topo = i * alturaLinha + (alturaLinha - (t.getAscent() + t.getDescent())) / 2f;
            t.draw(g, 0, topo + t.getAscent());
        }
        g.dispose();
    }
}
