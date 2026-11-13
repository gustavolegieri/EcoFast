package br.com.ecofast.ui.componentes;

import br.com.ecofast.ui.Tema;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * Painel branco de cantos arredondados, o "cartão" onde cada bloco da tela fica.
 *
 * O que se adiciona ao cartão vai para um painel interno que usa o layout informado.
 * A margem fica na borda do cartão (que é um BorderLayout): colocar uma borda direto
 * num painel com MigLayout faz o conteúdo ficar mais estreito do que deveria.
 */
public class Cartao extends JPanel {

    private final JPanel conteudo;
    private Color fundo = Color.WHITE;
    private Color borda = Tema.BORDA;
    private float espessura = 1f;
    private final int arco = 20;

    public Cartao(LayoutManager layout) {
        super(new BorderLayout());
        conteudo = new JPanel(layout);
        conteudo.setOpaque(false);
        super.addImpl(conteudo, BorderLayout.CENTER, -1);
        setOpaque(false);
        setBorder(new EmptyBorder(18, 22, 18, 22));
    }

    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        conteudo.add(comp, constraints, index);
    }

    @Override
    public void removeAll() {
        conteudo.removeAll();
    }

    public Cartao cores(Color fundo, Color borda, float espessura) {
        this.fundo = fundo;
        this.borda = borda;
        this.espessura = espessura;
        repaint();
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        float meio = espessura / 2f;
        RoundRectangle2D.Float forma = new RoundRectangle2D.Float(meio, meio, getWidth() - espessura,
                getHeight() - espessura, arco, arco);
        g2.setColor(fundo);
        g2.fill(forma);
        g2.setColor(borda);
        g2.setStroke(new BasicStroke(espessura));
        g2.draw(forma);
        g2.dispose();
        super.paintComponent(g);
    }
}
