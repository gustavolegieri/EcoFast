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

/**
 * Bloco da tela: fundo claro, linha de 2 px em tinta e cantos retos. Pode ter uma sombra
 * deslocada (só para o que merece foco, como o saldo ou o formulário de entrada).
 *
 * O que se adiciona vai para um painel interno que usa o layout informado. A margem fica na borda
 * do cartão (que é um BorderLayout): colocar uma borda direto num painel com MigLayout faz o
 * conteúdo ficar mais estreito do que deveria.
 */
public class Cartao extends JPanel {

    private final JPanel conteudo;
    private Color fundo = Tema.BRANCO;
    private Color borda = Tema.TINTA;
    private int espessura = 2;
    private int margemCima = 28;
    private int margemEsquerda = 28;
    private int margemBaixo = 28;
    private int margemDireita = 28;
    private int sombra;
    private Color corSombra = Tema.TINTA;

    public Cartao(LayoutManager layout) {
        super(new BorderLayout());
        conteudo = new JPanel(layout);
        conteudo.setOpaque(false);
        super.addImpl(conteudo, BorderLayout.CENTER, -1);
        setOpaque(false);
        atualizarBorda();
    }

    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        conteudo.add(comp, constraints, index);
    }

    @Override
    public void removeAll() {
        conteudo.removeAll();
    }

    public Cartao cores(Color fundo, Color borda) {
        this.fundo = fundo;
        this.borda = borda;
        repaint();
        return this;
    }

    /** Sombra deslocada para baixo e para a direita, "px" pixels, na cor informada. */
    public Cartao sombra(int px, Color cor) {
        this.sombra = px;
        this.corSombra = cor;
        atualizarBorda();
        return this;
    }

    /** Espaço entre a borda externa do cartão e o conteúdo: linha de 2 px mais o recuo (0 encosta o conteúdo na linha). */
    public Cartao margem(int px) {
        return margens(px, px, px, px);
    }

    public Cartao margens(int cima, int esquerda, int baixo, int direita) {
        this.margemCima = cima;
        this.margemEsquerda = esquerda;
        this.margemBaixo = baixo;
        this.margemDireita = direita;
        atualizarBorda();
        return this;
    }

    private void atualizarBorda() {
        setBorder(new EmptyBorder(margemCima, margemEsquerda, margemBaixo + sombra, margemDireita + sombra));
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int largura = getWidth() - sombra, altura = getHeight() - sombra;
        if (sombra > 0) {
            g2.setColor(corSombra);
            g2.fillRect(sombra, sombra, largura, altura);
        }
        g2.setColor(fundo);
        g2.fillRect(0, 0, largura, altura);
        g2.setColor(borda);
        g2.setStroke(new BasicStroke(espessura));
        g2.drawRect(espessura / 2, espessura / 2, largura - espessura, altura - espessura);
        g2.dispose();
        super.paintComponent(g);
    }
}
