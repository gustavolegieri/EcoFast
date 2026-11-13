package br.com.ecofast.ui.componentes;

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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.util.List;

/** Gráfico de barras desenhado em Java2D: quilos por dia. Sobe animado e mostra o valor ao passar o mouse. */
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
        int esquerda = 58, direita = 8, topo = 16, base = 30;
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
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Rectangle a = area();
        double passo = passoBonito(maximo() / 4);
        double escala = Math.ceil(maximo() / passo) * passo;

        g.setFont(Tema.fonte(11, Font.PLAIN));
        FontMetrics fm = g.getFontMetrics();
        for (double v = 0; v <= escala + 1e-9; v += passo) {
            int y = a.y + a.height - (int) Math.round(a.height * v / escala);
            g.setColor(Tema.BORDA);
            g.setStroke(new BasicStroke(1f));
            g.drawLine(a.x, y, a.x + a.width, y);
            String rotulo = Fmt.inteiro(Math.round(v)) + " kg";
            g.setColor(Tema.SUAVE);
            g.drawString(rotulo, a.x - 8 - fm.stringWidth(rotulo), y + fm.getAscent() / 2 - 1);
        }

        int n = valores.length;
        if (n == 0) {
            g.dispose();
            return;
        }
        double fatia = a.width / (double) n;
        double larguraBarra = Math.max(3, fatia * 0.62);
        g.setClip(a.x - 2, a.y - 6, a.width + 4, a.height + 6);
        for (int i = 0; i < n; i++) {
            double altura = a.height * valores[i] / escala * progresso;
            if (altura < 1) {
                continue;
            }
            double x = a.x + i * fatia + (fatia - larguraBarra) / 2;
            double y = a.y + a.height - altura;
            g.setColor(i == destaque ? Tema.VERDE_900 : Tema.VERDE_500);
            g.fill(new RoundRectangle2D.Double(x, y, larguraBarra, altura + 8, 8, 8));
        }
        g.setClip(null);

        g.setFont(Tema.fonte(11, Font.PLAIN));
        g.setColor(Tema.SUAVE);
        int cada = Math.max(1, (int) Math.ceil(n / 7.0));
        for (int i = 0; i < n; i += cada) {
            String rotulo = Fmt.dia(dias.get(i));
            double centro = a.x + i * fatia + fatia / 2;
            g.drawString(rotulo, (int) (centro - fm.stringWidth(rotulo) / 2.0), a.y + a.height + 20);
        }

        if (destaque >= 0) {
            String texto = Fmt.dia(dias.get(destaque)) + "  ·  " + Fmt.kg(valores[destaque]);
            g.setFont(Tema.fonte(12, Font.BOLD));
            FontMetrics fb = g.getFontMetrics();
            int largura = fb.stringWidth(texto) + 20, altura = 28;
            double centro = a.x + destaque * fatia + fatia / 2;
            double alturaBarra = a.height * valores[destaque] / escala * progresso;
            int bx = (int) Math.max(a.x, Math.min(centro - largura / 2.0, a.x + a.width - largura));
            int by = (int) Math.max(2, a.y + a.height - alturaBarra - altura - 8);
            g.setColor(new Color(0x1F2A1F));
            g.fill(new RoundRectangle2D.Double(bx, by, largura, altura, 10, 10));
            g.setColor(Color.WHITE);
            g.drawString(texto, bx + 10, by + 19);
        }
        g.dispose();
    }
}
