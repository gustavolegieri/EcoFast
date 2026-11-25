package br.com.ecofast.ui.componentes;

import br.com.ecofast.ui.Tema;

import javax.swing.border.AbstractBorder;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

/**
 * Borda dos campos de texto: linha de 2 px em tinta. Com o foco, um anel dourado de 3 px aparece por dentro
 * da linha (por fora ele mudaria o tamanho do campo, e o campo deve medir sempre o mesmo).
 */
public class BordaCampo extends AbstractBorder {

    private final int vertical;
    private final int horizontal;

    /** Espaço entre a linha e o texto, em pixels (a linha de 2 px vem a mais). */
    public BordaCampo(int vertical, int horizontal) {
        this.vertical = vertical;
        this.horizontal = horizontal;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int largura, int altura) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Tema.TINTA);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(x + 1, y + 1, largura - 2, altura - 2);
        if (c.hasFocus()) {
            g2.setColor(Tema.OURO);
            g2.setStroke(new BasicStroke(3f));
            g2.drawRect(x + 3 + 1, y + 3 + 1, largura - 8 - 1, altura - 8 - 1);
        }
        g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(2 + vertical, 2 + horizontal, 2 + vertical, 2 + horizontal);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.set(2 + vertical, 2 + horizontal, 2 + vertical, 2 + horizontal);
        return insets;
    }
}
