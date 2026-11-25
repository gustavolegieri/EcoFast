package br.com.ecofast.ui.componentes;

import br.com.ecofast.ui.Tema;

import javax.swing.border.AbstractBorder;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

/** Moldura tracejada de 1,5 px em tinta: marca uma informação de apoio que não é um campo nem um botão. */
public class BordaTracejada extends AbstractBorder {

    private final int espaco;

    public BordaTracejada(int espaco) {
        this.espaco = espaco;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int largura, int altura) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Tema.TINTA);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{4f, 3f}, 0f));
        g2.drawRect(x + 1, y + 1, largura - 3, altura - 3);
        g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(espaco, espaco + 4, espaco, espaco + 4);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.set(espaco, espaco + 4, espaco, espaco + 4);
        return insets;
    }
}
