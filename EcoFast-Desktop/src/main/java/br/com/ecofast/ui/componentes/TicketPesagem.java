package br.com.ecofast.ui.componentes;

import br.com.ecofast.model.material.Material;
import br.com.ecofast.service.ReciboDescarte;
import br.com.ecofast.ui.Fmt;
import br.com.ecofast.ui.Tema;
import net.miginfocom.swing.MigLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.time.format.DateTimeFormatter;

/**
 * O recibo da entrega, desenhado como o ticket que sai da balança: papel claro, faixa verde no topo,
 * tracejados, números em fonte monoespaçada e borda inferior serrilhada.
 */
public class TicketPesagem extends JPanel {

    private static final int SOMBRA = 6;
    private static final int DENTE_L = 18;
    private static final int DENTE_A = 9;

    public TicketPesagem() {
        super(new MigLayout("insets 30 26 " + (28 + DENTE_A + SOMBRA) + " " + (26 + SOMBRA) + ", wrap 2, fillx",
                "[grow, fill][grow, right]", ""));
        setOpaque(false);
    }

    /** Monta o ticket com os dados de uma entrega já gravada. */
    public void mostrar(ReciboDescarte r, String operador) {
        removeAll();
        Material m = r.descarte().getMaterial();

        JLabel marca = Tema.titulo("EcoFast", 28, Tema.TINTA);
        marca.setHorizontalAlignment(SwingConstants.CENTER);
        add(marca, "span 2, growx");
        add(centro(r.ecoponto() + " · Ticket de pesagem"), "span 2, growx");
        add(centro(r.descarte().getDataHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Fmt.ptBr()))
                + " · nº " + String.format("%06d", r.descarte().getId())), "span 2, growx");
        add(new Divisor(), "span 2, growx, gaptop 12, gapbottom 8, h 3!");

        linha("Cidadão", r.cidadao());
        linha("Material", Tema.nomeCurto(m));
        linha("Peso", Fmt.kg(r.descarte().getPesoKg()) + " × " + (int) m.getMultiplicador());
        linha("Operador", operador);
        add(new Divisor(), "span 2, growx, gaptop 8, gapbottom 10, h 3!");

        add(Tema.mono("Pontos ganhos", 14, Font.PLAIN, Tema.TINTA_SUAVE), "aligny center");
        add(Tema.mono("+" + Fmt.inteiro(r.descarte().getPontos()), 34, Font.PLAIN, Tema.TINTA));

        JPanel saldo = new JPanel(new MigLayout("insets 10 14 10 14, fillx", "[grow][]", "[]")) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(Tema.TINTA);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        saldo.setOpaque(false);
        saldo.add(Tema.mono("Novo saldo", 14, Font.PLAIN, Tema.PAPEL));
        saldo.add(Tema.mono(Fmt.inteiro(r.novoSaldo()), 22, Font.PLAIN, Tema.OURO));
        add(saldo, "span 2, growx, gaptop 10");

        JLabel nota = Tema.mono("Crédito lançado após conferência do funcionário.", 11, Font.PLAIN, Tema.TINTA_SUAVE);
        nota.setHorizontalAlignment(SwingConstants.CENTER);
        add(nota, "span 2, growx, gaptop 14");
        revalidate();
        repaint();
    }

    private JLabel centro(String texto) {
        JLabel l = Tema.mono(texto, 12, Font.PLAIN, Tema.TINTA_SUAVE);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    private void linha(String nome, String valor) {
        add(Tema.mono(nome, 14, Font.PLAIN, Tema.TINTA_SUAVE), "gaptop 3");
        add(Tema.mono(valor, 14, Font.PLAIN, Tema.TINTA), "gaptop 3");
    }

    /** Linha tracejada de 2 px que separa as partes do ticket. */
    private static final class Divisor extends JComponent {
        Divisor() {
            setPreferredSize(new Dimension(10, 3));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(Tema.TINTA);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{5f, 4f}, 0f));
            g2.drawLine(0, 1, getWidth(), 1);
            g2.dispose();
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int largura = getWidth() - SOMBRA, altura = getHeight() - SOMBRA;

        Polygon corpo = new Polygon();
        corpo.addPoint(0, 0);
        corpo.addPoint(largura, 0);
        corpo.addPoint(largura, altura - DENTE_A);
        boolean baixo = true;
        for (int x = largura; x > 0; x -= DENTE_L / 2) {
            corpo.addPoint(Math.max(0, x - DENTE_L / 2), baixo ? altura : altura - DENTE_A);
            baixo = !baixo;
        }
        corpo.addPoint(0, altura - DENTE_A);

        g.translate(SOMBRA, SOMBRA);
        g.setColor(Tema.TINTA);
        g.fillPolygon(corpo);
        g.translate(-SOMBRA, -SOMBRA);

        g.setColor(Tema.BRANCO);
        g.fillPolygon(corpo);
        g.setColor(Tema.VERDE);
        g.fillRect(0, 0, largura, 6);
        g.setColor(Tema.TINTA);
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
        g.drawPolygon(corpo);
        g.dispose();
        super.paintComponent(g0);
    }
}
