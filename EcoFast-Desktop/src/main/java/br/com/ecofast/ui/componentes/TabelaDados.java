package br.com.ecofast.ui.componentes;

import br.com.ecofast.ui.Tema;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
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
 * Tabela só de leitura, com o visual do EcoFast. Não tem barra de rolagem própria
 * (a tela inteira é que rola), as colunas dividem a largura em proporções que a
 * tela define, e quando está vazia mostra um aviso.
 */
public class TabelaDados extends JScrollPane {

    private final DefaultTableModel modelo;
    private final JTable tabela;
    private final String vazio;
    private int minimoDeLinhas = 3;
    private int[] pesos;

    public TabelaDados(String vazio, String[] colunas, int... colunasNumericas) {
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
                    g2.setColor(Tema.SUAVE);
                    int largura = g2.getFontMetrics().stringWidth(TabelaDados.this.vazio);
                    g2.drawString(TabelaDados.this.vazio, Math.max(8, (getWidth() - largura) / 2), 34);
                    g2.dispose();
                }
            }
        };
        tabela.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabela.setRowHeight(34);
        tabela.setShowVerticalLines(false);
        tabela.setShowHorizontalLines(true);
        tabela.setGridColor(Tema.BORDA);
        tabela.setIntercellSpacing(new Dimension(0, 1));
        tabela.setFocusable(false);
        tabela.setRowSelectionAllowed(false);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setFillsViewportHeight(true);
        tabela.setBackground(Color.WHITE);
        tabela.setFont(Tema.fonte(14, Font.PLAIN));
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.getTableHeader().setResizingAllowed(false);

        final int[] numericas = colunasNumericas;
        DefaultTableCellRenderer celula = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel, boolean foco, int l, int c) {
                super.getTableCellRendererComponent(t, valor, false, false, l, c);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                setHorizontalAlignment(ehNumerica(numericas, c) ? SwingConstants.RIGHT : SwingConstants.LEFT);
                setForeground(Tema.TEXTO);
                return this;
            }
        };
        for (int i = 0; i < colunas.length; i++) {
            tabela.getColumnModel().getColumn(i).setCellRenderer(celula);
        }
        tabela.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel, boolean foco, int l, int c) {
                JLabel r = (JLabel) super.getTableCellRendererComponent(t, valor == null ? "" : valor.toString().toUpperCase(),
                        false, false, l, c);
                r.setFont(Tema.fonte(11, Font.BOLD));
                r.setForeground(Tema.SUAVE);
                r.setBackground(Color.WHITE);
                r.setBorder(new javax.swing.border.CompoundBorder(
                        new javax.swing.border.MatteBorder(0, 0, 2, 0, Tema.BORDA), new EmptyBorder(8, 10, 8, 10)));
                r.setHorizontalAlignment(ehNumerica(numericas, c) ? SwingConstants.RIGHT : SwingConstants.LEFT);
                return r;
            }
        });

        setViewportView(tabela);
        // Uma borda vazia (e não null): com null o JTable troca por uma moldura cinza ao ser adicionado.
        setBorder(BorderFactory.createEmptyBorder());
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER);
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        getViewport().setBackground(Color.WHITE);
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

    private static boolean ehNumerica(int[] numericas, int coluna) {
        return Arrays.stream(numericas).anyMatch(n -> n == coluna);
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

    /** Altura mínima da tabela, em linhas, para os cartões ao lado não ficarem desproporcionais. */
    public void alturaParaLinhas(int linhas) {
        this.minimoDeLinhas = linhas;
        recalcularAltura();
    }

    private void recalcularAltura() {
        int linhas = Math.max(minimoDeLinhas, modelo.getRowCount());
        int cabecalho = tabela.getTableHeader().getPreferredSize().height;
        setPreferredSize(new Dimension(300, cabecalho + linhas * (tabela.getRowHeight() + 1) + 2));
        revalidate();
    }
}
