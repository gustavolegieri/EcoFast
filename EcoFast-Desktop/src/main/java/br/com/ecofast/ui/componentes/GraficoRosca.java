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
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/** Gráfico de rosca em Java2D: quilos por material. A fatia sob o mouse cresce e detalha o valor no centro. */
public class GraficoRosca extends JComponent {

    public record Fatia(String nome, Color cor, double valor) {
    }

    private List<Fatia> fatias = List.of();
    private double progresso = 1;
    private int destaque = -1;
    private Timer animacao;

    public GraficoRosca() {
        setOpaque(false);
        setPreferredSize(new Dimension(420, 300));
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int i = fatiaEm(e.getPoint());
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

    public void setDados(List<Fatia> fatias) {
        this.fatias = fatias;
        this.destaque = -1;
        if (animacao != null) {
            animacao.stop();
        }
        progresso = 0;
        long inicio = System.nanoTime();
        animacao = new Timer(15, e -> {
            double t = Math.min(1, (System.nanoTime() - inicio) / 900e6);
            progresso = 1 - Math.pow(1 - t, 3);
            repaint();
            if (t >= 1) {
                ((Timer) e.getSource()).stop();
            }
        });
        animacao.start();
    }

    private double total() {
        return fatias.stream().mapToDouble(Fatia::valor).sum();
    }

    private Rectangle2D.Double rosca() {
        double lado = Math.max(60, Math.min(getHeight() - 28, getWidth() * 0.52));
        return new Rectangle2D.Double(14, (getHeight() - lado) / 2, lado, lado);
    }

    private int fatiaEm(Point p) {
        double total = total();
        if (total <= 0) {
            return -1;
        }
        Rectangle2D.Double r = rosca();
        double cx = r.getCenterX(), cy = r.getCenterY(), raio = Math.hypot(p.x - cx, p.y - cy);
        if (raio > r.width / 2 + 6 || raio < r.width * 0.30) {
            return -1;
        }
        double graus = Math.toDegrees(Math.atan2(-(p.y - cy), p.x - cx));
        double dePartida = (90 - graus + 720) % 360;
        double acumulado = 0;
        for (int i = 0; i < fatias.size(); i++) {
            acumulado += 360 * fatias.get(i).valor() / total;
            if (dePartida < acumulado) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        double total = total();
        Rectangle2D.Double r = rosca();
        double cx = r.getCenterX(), cy = r.getCenterY();
        double furo = r.width * 0.30;

        if (total <= 0) {
            Area vazio = new Area(new Ellipse2D.Double(r.x, r.y, r.width, r.height));
            vazio.subtract(new Area(new Ellipse2D.Double(cx - furo, cy - furo, furo * 2, furo * 2)));
            g.setColor(Tema.VERDE_100);
            g.fill(vazio);
            centralizar(g, "Sem entregas", "no período", cx, cy);
            g.dispose();
            return;
        }

        double inicio = 90;
        for (int i = 0; i < fatias.size(); i++) {
            Fatia f = fatias.get(i);
            double extensao = -360 * f.valor() / total * progresso;
            double cresce = i == destaque ? 7 : 0;
            Area parte = new Area(new Arc2D.Double(r.x - cresce, r.y - cresce, r.width + cresce * 2, r.height + cresce * 2,
                    inicio, extensao, Arc2D.PIE));
            parte.subtract(new Area(new Ellipse2D.Double(cx - furo, cy - furo, furo * 2, furo * 2)));
            g.setColor(f.cor());
            g.fill(parte);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2.5f));
            g.draw(parte);
            inicio += extensao;
        }

        if (destaque >= 0 && destaque < fatias.size()) {
            Fatia f = fatias.get(destaque);
            centralizar(g, f.nome(), Fmt.kg(f.valor()) + " · " + Math.round(100 * f.valor() / total) + "%", cx, cy);
        } else {
            centralizar(g, Fmt.kg(total * progresso), "no período", cx, cy);
        }

        double legendaX = r.x + r.width + 34;
        double largura = getWidth() - legendaX - 8;
        double y = Math.max(16, cy - fatias.size() * 15);
        FontMetrics fm;
        for (int i = 0; i < fatias.size(); i++) {
            Fatia f = fatias.get(i);
            double linhaY = y + i * 30;
            g.setColor(f.cor());
            g.fill(new RoundRectangle2D.Double(legendaX, linhaY + 3, 14, 14, 5, 5));
            g.setFont(Tema.fonte(13, i == destaque ? Font.BOLD : Font.PLAIN));
            g.setColor(Tema.TEXTO);
            g.drawString(f.nome(), (float) (legendaX + 22), (float) (linhaY + 16));
            String valor = Fmt.kg(f.valor());
            g.setFont(Tema.fonte(12, Font.BOLD));
            fm = g.getFontMetrics();
            g.setColor(Tema.SUAVE);
            if (largura > 140) {
                g.drawString(valor, (float) (legendaX + largura - fm.stringWidth(valor)), (float) (linhaY + 16));
            }
        }
        g.dispose();
    }

    private void centralizar(Graphics2D g, String linha1, String linha2, double cx, double cy) {
        g.setFont(Tema.fonte(18, Font.BOLD));
        FontMetrics f1 = g.getFontMetrics();
        g.setColor(Tema.VERDE_900);
        g.drawString(linha1, (float) (cx - f1.stringWidth(linha1) / 2.0), (float) (cy + 2));
        g.setFont(Tema.fonte(12, Font.PLAIN));
        FontMetrics f2 = g.getFontMetrics();
        g.setColor(Tema.SUAVE);
        g.drawString(linha2, (float) (cx - f2.stringWidth(linha2) / 2.0), (float) (cy + 20));
    }
}
