package br.com.ecofast.ui.componentes;

import javax.swing.Icon;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Pontinho colorido usado como marcador e como "cor" de cada material. */
public class Bolinha implements Icon {

    private final Color cor;
    private final int diametro;

    public Bolinha(Color cor, int diametro) {
        this.cor = cor;
        this.diametro = diametro;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(cor);
        g2.fillOval(x, y, diametro, diametro);
        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return diametro;
    }

    @Override
    public int getIconHeight() {
        return diametro;
    }
}
