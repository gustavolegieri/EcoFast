package br.com.ecofast.ui.componentes;

import br.com.ecofast.ui.Tema;
import br.com.ecofast.ui.Texto;
import net.miginfocom.swing.MigLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;

/**
 * Faixa de indicadores: uma caixa só, dividida por linhas verticais. Cada célula tem um nome pequeno,
 * o número grande em fonte monoespaçada (com a unidade ao lado, em fonte de texto) e, se quiser, uma nota embaixo.
 */
public class FaixaKpi extends JPanel {

    private final Valor[] valores;
    private final Texto[] notas;

    /** Número de 40 px, margem de 20 px em cima e embaixo (a faixa do painel do administrador). */
    public FaixaKpi(String... titulos) {
        this(titulos, new String[titulos.length], 40, 20);
    }

    public FaixaKpi(String[] titulos, String[] notasIniciais, float tamanhoNumero, int margemVertical) {
        super(new MigLayout("insets 0, fill, gap 0", "[grow, fill, sg k]".repeat(titulos.length), "[grow, fill]"));
        setOpaque(false);
        valores = new Valor[titulos.length];
        notas = new Texto[titulos.length];
        for (int i = 0; i < titulos.length; i++) {
            // 2 px de linha + o recuo da página (a primeira célula já parte da borda de fora; as outras, do divisor)
            JPanel celula = new JPanel(new MigLayout("insets " + (margemVertical + 2) + " " + (i == 0 ? 28 : 26) + " "
                    + (margemVertical + 2) + " 28, gap 0, wrap 1, fillx, hidemode 3", "[grow, fill]", ""));
            celula.setOpaque(false);
            celula.add(Tema.kicker(titulos[i], Tema.VERDE), "h 18!");
            valores[i] = new Valor(tamanhoNumero);
            celula.add(valores[i], "gaptop 6");
            notas[i] = Tema.rotulo(notasIniciais[i] == null ? " " : notasIniciais[i], 14, Font.PLAIN, Tema.TINTA_SUAVE).linha(21);
            notas[i].setVisible(notasIniciais[i] != null);
            celula.add(notas[i], "gaptop 2");
            add(celula);
        }
    }

    public void setValor(int indice, String numero) {
        valores[indice].definir(numero, null);
    }

    /** Número com a unidade ao lado ("520,4" e "kg"). */
    public void setValor(int indice, String numero, String unidade) {
        valores[indice].definir(numero, unidade);
    }

    public void setNota(int indice, String texto) {
        notas[indice].setText(texto);
        notas[indice].setVisible(texto != null && !texto.isBlank());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Tema.BRANCO);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(Tema.TINTA);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(1, 1, getWidth() - 2, getHeight() - 2);
        int n = valores.length;
        for (int i = 1; i < n; i++) {
            int x = (int) Math.round(getWidth() * i / (double) n);
            g2.fillRect(x - 1, 0, 2, getHeight());
        }
        g2.dispose();
        super.paintComponent(g);
    }

    /** O número grande: DM Mono 500 com altura de linha 1,1, e a unidade em Archivo 600 de 18 px, 6 px depois. */
    private static final class Valor extends JComponent {
        private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
        private final float tamanho;
        private String numero = "--";
        private String unidade;

        Valor(float tamanho) {
            this.tamanho = tamanho;
            setOpaque(false);
        }

        void definir(String numero, String unidade) {
            this.numero = numero;
            this.unidade = unidade;
            revalidate();
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            Font f = Tema.fonteMono(tamanho, Font.BOLD);
            double w = f.getStringBounds(numero, FRC).getWidth();
            if (unidade != null) {
                w += 6 + Tema.fontePeso(18, 600).getStringBounds(unidade, FRC).getWidth();
            }
            return new Dimension((int) Math.ceil(w) + 2, Math.round(tamanho * 1.1f));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            Font f = Tema.fonteMono(tamanho, Font.BOLD);
            LineMetrics lm = f.getLineMetrics(numero, FRC);
            double base = (getHeight() - (lm.getAscent() + lm.getDescent())) / 2.0 + lm.getAscent();
            g.setFont(f);
            g.setColor(Tema.TINTA);
            g.drawString(numero, 0f, (float) base);
            if (unidade != null) {
                double x = f.getStringBounds(numero, FRC).getWidth() + 6;
                g.setFont(Tema.fontePeso(18, 600));
                g.drawString(unidade, (float) x, (float) base);
            }
            g.dispose();
        }
    }
}
