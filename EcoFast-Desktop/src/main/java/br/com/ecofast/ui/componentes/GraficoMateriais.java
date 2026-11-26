package br.com.ecofast.ui.componentes;

import br.com.ecofast.Idioma;
import br.com.ecofast.ui.Fmt;
import br.com.ecofast.ui.Tema;

import javax.swing.JComponent;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.List;

/**
 * Quilos por material em barras horizontais: o nome, o valor e a fatia do total ficam escritos em cada
 * linha, então a cor ajuda mas nunca é o único sinal. Substitui a rosca, que dependia de comparar ângulos
 * e de uma legenda separada.
 */
public class GraficoMateriais extends JComponent {

    public record Fatia(String nome, Color cor, double valor) {
    }

    private static final int ALTURA_LINHA = 58;

    private List<Fatia> fatias = List.of();
    private double progresso = 1;
    private int destaque = -1;
    private Timer animacao;

    public GraficoMateriais() {
        setOpaque(false);
        setPreferredSize(new Dimension(420, 5 * ALTURA_LINHA));
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int i = e.getY() / ALTURA_LINHA;
                i = i >= 0 && i < fatias.size() ? i : -1;
                if (i != destaque) {
                    destaque = i;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                destaque = -1;
                repaint();
            }
        };
        addMouseMotionListener(mouse);
        addMouseListener(mouse);
    }

    public void setDados(List<Fatia> dados) {
        this.fatias = dados.stream().sorted(Comparator.comparingDouble(Fatia::valor).reversed()).toList();
        this.destaque = -1;
        setPreferredSize(new Dimension(420, Math.max(1, fatias.size()) * ALTURA_LINHA));
        revalidate();
        if (animacao != null) {
            animacao.stop();
        }
        progresso = 0;
        long inicio = System.nanoTime();
        animacao = new Timer(15, e -> {
            double t = Math.min(1, (System.nanoTime() - inicio) / 800e6);
            progresso = 1 - Math.pow(1 - t, 3);
            repaint();
            if (t >= 1) {
                ((Timer) e.getSource()).stop();
            }
        });
        animacao.start();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        double total = fatias.stream().mapToDouble(Fatia::valor).sum();
        double maior = fatias.stream().mapToDouble(Fatia::valor).max().orElse(1);

        if (total <= 0) {
            g.setFont(Tema.fonte(14, Font.PLAIN));
            g.setColor(Tema.TINTA_SUAVE);
            g.drawString(Idioma.t("Sem entregas no período.", "No deliveries in this period."), 0, 24);
            g.dispose();
            return;
        }

        for (int i = 0; i < fatias.size(); i++) {
            Fatia f = fatias.get(i);
            int topo = i * ALTURA_LINHA;
            if (i > 0) {
                Tema.tracejado(g, 0, getWidth(), topo);
            }
            boolean sobre = i == destaque;

            new Marcador(f.cor(), 14).paintIcon(this, g, 0, topo + 14);
            g.setFont(Tema.fonte(16, Font.BOLD));
            g.setColor(Tema.TINTA);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(f.nome(), 24, topo + 14 + (14 - fm.getHeight()) / 2 + fm.getAscent());

            String valor = Fmt.kg(f.valor()) + " · " + Fmt.decimal(100 * f.valor() / total) + "%";
            g.setFont(Tema.fonteMono(14, sobre ? Font.BOLD : Font.PLAIN));
            FontMetrics fv = g.getFontMetrics();
            g.drawString(valor, getWidth() - fv.stringWidth(valor), topo + 14 + (14 - fv.getHeight()) / 2 + fv.getAscent());

            int trilhaY = topo + 36, trilhaAltura = 16;
            g.setColor(Tema.TINTA);
            g.setStroke(new BasicStroke(2f));
            g.drawRect(1, trilhaY, getWidth() - 3, trilhaAltura);
            int cheio = (int) Math.round((getWidth() - 4) * (f.valor() / maior) * progresso);
            g.setColor(f.cor());
            g.fillRect(2, trilhaY + 1, cheio, trilhaAltura - 1);
        }
        g.dispose();
    }
}
