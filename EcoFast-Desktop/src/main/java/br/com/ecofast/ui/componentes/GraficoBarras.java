package br.com.ecofast.ui.componentes;

import br.com.ecofast.ui.Fmt;
import br.com.ecofast.ui.Tema;

import javax.swing.JComponent;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.List;

/**
 * Gráfico de barras desenhado em Java2D: quilos por dia. Barras retas em verde, a do dia de pico em ouro,
 * linhas de grade tracejadas e a base em tinta. Sobe animado e mostra o valor ao passar o mouse.
 */
public class GraficoBarras extends JComponent {

    private List<LocalDate> dias = List.of();
    private double[] valores = new double[0];
    private double progresso = 1;
    private int destaque = -1;
    private Timer animacao;

    public GraficoBarras() {
        setOpaque(false);
        setPreferredSize(new Dimension(520, 300));
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int i = indiceEm(e.getPoint());
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

    public void setDados(List<LocalDate> dias, double[] valores) {
        this.dias = dias;
        this.valores = valores;
        this.destaque = -1;
        animar();
    }

    /** Índice do dia de maior volume, ou -1 se nenhum dia teve entrega. */
    public int diaDePico() {
        int melhor = -1;
        for (int i = 0; i < valores.length; i++) {
            if (valores[i] > 0 && (melhor < 0 || valores[i] > valores[melhor])) {
                melhor = i;
            }
        }
        return melhor;
    }

    private void animar() {
        if (animacao != null) {
            animacao.stop();
        }
        progresso = 0;
        long inicio = System.nanoTime();
        animacao = new Timer(15, e -> {
            double t = Math.min(1, (System.nanoTime() - inicio) / 700e6);
            progresso = 1 - Math.pow(1 - t, 3);
            repaint();
            if (t >= 1) {
                ((Timer) e.getSource()).stop();
            }
        });
        animacao.start();
    }

    private Rectangle area() {
        int esquerda = 70, direita = 4, topo = 14, base = 32;
        return new Rectangle(esquerda, topo, Math.max(10, getWidth() - esquerda - direita),
                Math.max(10, getHeight() - topo - base));
    }

    private int indiceEm(Point p) {
        Rectangle a = area();
        if (valores.length == 0 || !a.contains(p)) {
            return -1;
        }
        return Math.min(valores.length - 1, (int) ((p.x - a.x) / (a.width / (double) valores.length)));
    }

    private double maximo() {
        double max = 1;
        for (double v : valores) {
            max = Math.max(max, v);
        }
        return max;
    }

    /** Passo "redondo" para as linhas do eixo: 1, 2, 5, 10, 20, 50... */
    private static double passoBonito(double bruto) {
        double magnitude = Math.pow(10, Math.floor(Math.log10(bruto)));
        double normal = bruto / magnitude;
        double passo = normal <= 1 ? 1 : normal <= 2 ? 2 : normal <= 5 ? 5 : 10;
        return passo * magnitude;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Rectangle a = area();
        double passo = passoBonito(maximo() / 4);
        double escala = Math.ceil(maximo() / passo) * passo;

        g.setFont(Tema.fonteMono(12, Font.PLAIN));
        FontMetrics fm = g.getFontMetrics();
        for (double v = 0; v <= escala + 1e-9; v += passo) {
            int y = a.y + a.height - (int) Math.round(a.height * v / escala);
            if (v > 0) {
                Tema.tracejado(g, a.x, a.x + a.width, y);
            }
            String rotulo = Fmt.inteiro(Math.round(v)) + (v + passo > escala + 1e-9 ? " kg" : "");
            g.setColor(Tema.TINTA_SUAVE);
            g.drawString(rotulo, a.x - 12 - fm.stringWidth(rotulo), y + fm.getAscent() / 2 - 1);
        }

        int n = valores.length;
        int pico = diaDePico();
        if (n > 0) {
            double fatia = a.width / (double) n;
            double larguraBarra = Math.max(3, fatia - 4);
            for (int i = 0; i < n; i++) {
                double altura = a.height * valores[i] / escala * progresso;
                if (altura < 1) {
                    continue;
                }
                int x = (int) Math.round(a.x + i * fatia + (fatia - larguraBarra) / 2);
                int y = (int) Math.round(a.y + a.height - altura);
                g.setColor(i == destaque ? Tema.TINTA : i == pico ? Tema.OURO : Tema.VERDE);
                g.fillRect(x, y, (int) Math.round(larguraBarra), (int) Math.round(altura));
            }
        }

        g.setColor(Tema.TINTA);
        g.setStroke(new BasicStroke(2f));
        g.drawLine(a.x, a.y + a.height, a.x + a.width, a.y + a.height);

        if (n > 0) {
            double fatia = a.width / (double) n;
            g.setFont(Tema.fonteMono(12, Font.PLAIN));
            g.setColor(Tema.TINTA_SUAVE);
            int cada = Math.max(1, (int) Math.ceil(n / 6.0));
            for (int i = 0; i < n; i += cada) {
                String rotulo = Fmt.dia(dias.get(i));
                double centro = a.x + i * fatia + fatia / 2;
                g.drawString(rotulo, (int) Math.max(a.x, centro - fm.stringWidth(rotulo) / 2.0), a.y + a.height + 22);
            }

            if (destaque >= 0) {
                String texto = Fmt.dia(dias.get(destaque)) + "  ·  " + Fmt.kg(valores[destaque]);
                g.setFont(Tema.fonteMono(13, Font.BOLD));
                FontMetrics fb = g.getFontMetrics();
                int largura = fb.stringWidth(texto) + 22, altura = 30;
                double centro = a.x + destaque * fatia + fatia / 2;
                double alturaBarra = a.height * valores[destaque] / escala * progresso;
                int bx = (int) Math.max(a.x, Math.min(centro - largura / 2.0, a.x + a.width - largura));
                int by = (int) Math.max(2, a.y + a.height - alturaBarra - altura - 8);
                g.setColor(Tema.TINTA);
                g.fillRect(bx, by, largura, altura);
                g.setColor(Tema.PAPEL);
                g.drawString(texto, bx + 11, by + (altura - fb.getHeight()) / 2 + fb.getAscent());
            }
        }
        g.dispose();
    }
}
