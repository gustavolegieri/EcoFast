package br.com.ecofast.ui.componentes;

import br.com.ecofast.ui.Tema;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

/** A moeda dourada que representa as EcoCoins. */
public class MoedaIcone implements Icon {

    private final int lado;

    public MoedaIcone(int lado) {
        this.lado = lado;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(x, y);
        g2.setColor(new Color(0xC77F00));
        g2.fill(new Ellipse2D.Double(0, 0, lado, lado));
        g2.setColor(Tema.MOEDA_COR);
        g2.fill(new Ellipse2D.Double(lado * 0.07, lado * 0.07, lado * 0.86, lado * 0.86));
        g2.setColor(new Color(0xE69500));
        g2.setStroke(new BasicStroke(Math.max(1.5f, lado * 0.08f)));
        double d = lado * 0.54;
        g2.draw(new Ellipse2D.Double((lado - d) / 2, (lado - d) / 2, d, d));
        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return lado;
    }

    @Override
    public int getIconHeight() {
        return lado;
    }
}
