package br.com.ecofast.ui.componentes;

import br.com.ecofast.ui.Tema;
import br.com.ecofast.ui.Texto;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.AbstractBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Arrays;
import java.util.List;

/**
 * Tabela só de leitura, no estilo do recibo e com as medidas da página: cabeçalho de 35 px em caixa-alta
 * pequena com linha de tinta, linhas de 46 px separadas por tracejado, números em fonte monoespaçada.
 * Não tem barra de rolagem própria (a tela inteira é que rola), as colunas dividem a largura em proporções
 * que a tela define e, quando está vazia, mostra um aviso.
 */
public class TabelaDados extends JScrollPane {

    /** Valor de célula que mostra o quadradinho da cor do material ao lado do nome. */
    public record CelulaMaterial(String nome, Color cor) {
        @Override
        public String toString() {
            return nome;
        }
    }

    private static final int ALTURA_LINHA = 47;
    private static final int ALTURA_CABECALHO = 35;

    private final DefaultTableModel modelo;
    private final JTable tabela;
    private final String vazio;
    private int minimoDeLinhas = 3;
    private int[] pesos;

    /** colunasNumericas: alinhadas à direita e em fonte monoespaçada. */
    public TabelaDados(String vazio, String[] colunas, int... colunasNumericas) {
        this(vazio, colunas, new int[0], colunasNumericas);
    }

    /** colunasMono: texto em fonte monoespaçada alinhado à esquerda (datas, posições). */
    public TabelaDados(String vazio, String[] colunas, int[] colunasMono, int[] colunasNumericas) {
        this.vazio = vazio;
        modelo = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        tabela = new JTable(modelo) {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                if (getRowCount() == 0) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setFont(Tema.fonte(14, Font.PLAIN));
                    g2.setColor(Tema.TINTA_SUAVE);
                    int largura = g2.getFontMetrics().stringWidth(TabelaDados.this.vazio);
                    g2.drawString(TabelaDados.this.vazio, Math.max(8, (getWidth() - largura) / 2), 34);
                    g2.dispose();
                }
            }
        };
        tabela.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabela.setRowHeight(ALTURA_LINHA);
        tabela.setShowGrid(false);
        tabela.setIntercellSpacing(new Dimension(0, 0));
        tabela.setFocusable(false);
        tabela.setRowSelectionAllowed(false);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setFillsViewportHeight(true);
        tabela.setBackground(Tema.BRANCO);
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.getTableHeader().setResizingAllowed(false);

        TableCellRenderer celula = (t, valor, sel, foco, l, c) -> {
            boolean numerica = ehDa(colunasNumericas, c);
            boolean mono = numerica || ehDa(colunasMono, c);
            String texto = valor == null ? "" : valor.toString();
            Texto r = new Texto(texto);
            r.setOpaque(true);
            r.setBackground(Tema.BRANCO);
            r.linha(22.5f);
            r.setHorizontalAlignment(numerica ? SwingConstants.RIGHT : SwingConstants.LEFT);
            r.setBorder(new CompoundBorder(new LinhaTracejada(l < t.getRowCount() - 1), new EmptyBorder(0, 0, 2, 0)));
            if (valor instanceof CelulaMaterial m) {
                r.setIcon(new Marcador(m.cor(), 12));
                r.setIconTextGap(8);
                r.setFont(Tema.fontePeso(15, 600));
                r.setForeground(Tema.TINTA);
            } else if (mono) {
                boolean ganho = texto.startsWith("+");
                r.setFont(Tema.fonteMono(15, ganho ? Font.BOLD : Font.PLAIN));
                r.setForeground(ganho ? Tema.VERDE : Tema.TINTA);
            } else {
                r.setFont(Tema.fonte(15, Font.PLAIN));
                r.setForeground(Tema.TINTA);
            }
            return r;
        };
        for (int i = 0; i < colunas.length; i++) {
            tabela.getColumnModel().getColumn(i).setCellRenderer(celula);
        }
        tabela.getTableHeader().setDefaultRenderer((t, valor, sel, foco, l, c) -> {
            Texto r = new Texto(valor == null ? "" : valor.toString().toUpperCase());
            r.setFont(Tema.fonteMono(11.5f, Font.PLAIN, 0.08f));
            r.setForeground(Tema.TINTA_SUAVE);
            r.setOpaque(true);
            r.setBackground(Tema.BRANCO);
            r.linha(17.25f);
            r.setBorder(new CompoundBorder(new LinhaDeBaixo(), new EmptyBorder(8, 0, 8, 0)));
            r.setHorizontalAlignment(ehDa(colunasNumericas, c) ? SwingConstants.RIGHT : SwingConstants.LEFT);
            return r;
        });
        tabela.getTableHeader().setPreferredSize(new Dimension(100, ALTURA_CABECALHO));

        setViewportView(tabela);
        // A tabela não rola sozinha, mas o JScrollPane engoliria a roda do mouse: repassa para a tela.
        setWheelScrollingEnabled(false);
        addMouseWheelListener(e -> getParent().dispatchEvent(SwingUtilities.convertMouseEvent(this, e, getParent())));
        // Uma borda vazia (e não null): com null o JTable troca por uma moldura cinza ao ser adicionado.
        setBorder(BorderFactory.createEmptyBorder());
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER);
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        getViewport().setBackground(Tema.BRANCO);
        setOpaque(false);
        getViewport().setOpaque(false);
        getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                ajustarLarguras();
            }
        });
        recalcularAltura();
    }

    /** Borda de baixo tracejada, a que separa as linhas (a última linha não leva). */
    private static final class LinhaTracejada extends AbstractBorder {
        private final boolean ligada;

        LinhaTracejada(boolean ligada) {
            this.ligada = ligada;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int largura, int altura) {
            if (!ligada) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            Tema.tracejado(g2, x, x + largura, y + altura - 1);
            g2.dispose();
        }
    }

    /** Linha de 2 px em tinta embaixo do cabeçalho. */
    private static final class LinhaDeBaixo extends AbstractBorder {
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int largura, int altura) {
            g.setColor(Tema.TINTA);
            g.fillRect(x, y + altura - 2, largura, 2);
        }

        @Override
        public java.awt.Insets getBorderInsets(Component c) {
            return new java.awt.Insets(0, 0, 2, 0);
        }
    }

    private static boolean ehDa(int[] colunas, int coluna) {
        return Arrays.stream(colunas).anyMatch(n -> n == coluna);
    }

    @Override
    public void doLayout() {
        super.doLayout();
        ajustarLarguras();
    }

    /** Define quanto da largura cada coluna ocupa (só as proporções importam). */
    public void larguras(int... pesos) {
        this.pesos = pesos;
        ajustarLarguras();
    }

    private void ajustarLarguras() {
        int total = getViewport().getWidth();
        if (pesos == null || total <= 0) {
            return;
        }
        int soma = Arrays.stream(pesos).sum();
        int usado = 0;
        for (int i = 0; i < pesos.length && i < tabela.getColumnCount(); i++) {
            int w = i == pesos.length - 1 ? total - usado : total * pesos[i] / soma;
            tabela.getColumnModel().getColumn(i).setPreferredWidth(w);
            tabela.getColumnModel().getColumn(i).setWidth(w);
            usado += w;
        }
    }

    public void setLinhas(List<Object[]> linhas) {
        modelo.setRowCount(0);
        linhas.forEach(modelo::addRow);
        recalcularAltura();
    }

    public int quantidadeDeLinhas() {
        return modelo.getRowCount();
    }

    /** Altura mínima da tabela, em linhas, para os blocos ao lado não ficarem desproporcionais. */
    public void alturaParaLinhas(int linhas) {
        this.minimoDeLinhas = linhas;
        recalcularAltura();
    }

    private void recalcularAltura() {
        int linhas = Math.max(minimoDeLinhas, modelo.getRowCount());
        setPreferredSize(new Dimension(300, ALTURA_CABECALHO + linhas * ALTURA_LINHA));
        revalidate();
    }
}
