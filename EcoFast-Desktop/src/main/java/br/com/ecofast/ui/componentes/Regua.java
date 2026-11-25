package br.com.ecofast.ui.componentes;

import br.com.ecofast.ui.Tema;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** Régua de 10 blocos de 18 px com 3 px de folga: cada bloco cheio vale 10% do caminho até o desconto no IPTU. */
public class Regua extends JComponent {

    private static final int BLOCOS = 10;
    private static final int FOLGA = 3;

    private int percentual;

    public Regua() {
        setOpaque(false);
        setPreferredSize(new Dimension(200, 18));
    }

    public void setPercentual(int percentual) {
        this.percentual = Math.max(0, Math.min(100, percentual));
        setToolTipText(this.percentual + "% da meta");
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int cheios = percentual / 10;
        double largura = (getWidth() - FOLGA * (BLOCOS - 1)) / (double) BLOCOS;
        for (int i = 0; i < BLOCOS; i++) {
            int x = (int) Math.round(i * (largura + FOLGA));
            int w = (int) Math.round((i + 1) * (largura + FOLGA) - FOLGA) - x;
            if (i < cheios) {
                g2.setColor(Tema.FOLHA);
                g2.fillRect(x, 0, w, getHeight());
            }
            g2.setColor(Tema.TINTA);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRect(x, 0, w - 1, getHeight() - 1);
        }
        g2.dispose();
    }
}
