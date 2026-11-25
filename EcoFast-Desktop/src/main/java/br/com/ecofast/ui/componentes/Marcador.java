package br.com.ecofast.ui.componentes;

import br.com.ecofast.ui.Tema;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** Quadradinho com linha em tinta: marca a cor de cada material e o estado da conexão. */
public class Marcador implements Icon {

    private final Color cor;
    private final Color linha;
    private final int lado;

    public Marcador(Color cor, int lado) {
        this(cor, Tema.TINTA, lado);
    }

    public Marcador(Color cor, Color linha, int lado) {
        this.cor = cor;
        this.linha = linha;
        this.lado = lado;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(cor);
        g2.fillRect(x, y, lado, lado);
        g2.setColor(linha);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRect(x, y, lado - 1, lado - 1);
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
