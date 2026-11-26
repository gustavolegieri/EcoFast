package br.com.ecofast.ui;

import br.com.ecofast.Idioma;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.function.Consumer;

/** Chave "PT | EN": o idioma em uso aparece preenchido em tinta, o outro só com o contorno. */
final class SeletorIdioma extends JPanel {

    SeletorIdioma(Consumer<Idioma> aoEscolher) {
        super(new GridLayout(1, 2));
        setOpaque(false);
        for (Idioma idioma : Idioma.values()) {
            Opcao opcao = new Opcao(idioma);
            opcao.addActionListener(e -> {
                if (idioma != Idioma.atual()) {
                    aoEscolher.accept(idioma);
                }
            });
            add(opcao);
        }
    }

    private static final class Opcao extends JButton {
        private final Idioma idioma;

        Opcao(Idioma idioma) {
            super(idioma.name());
            this.idioma = idioma;
            setName("idioma-" + idioma.name());
            String nome = idioma == Idioma.PT ? "Português" : "English";
            setToolTipText(nome);
            getAccessibleContext().setAccessibleName(nome + (idioma == Idioma.atual() ? " ✓" : ""));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(Tema.fonteMono(13, Font.BOLD));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(44, 36);
        }

        @Override
        public Dimension getMinimumSize() {
            // sem isso o FlatLaf impõe a largura mínima de um botão comum e a chave não cabe no canto
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            boolean ativo = idioma == Idioma.atual();
            Color fundo = ativo ? Tema.TINTA : getModel().isRollover() ? Tema.PAPEL_2 : Tema.PAPEL;
            g.setColor(fundo);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Tema.TINTA);
            g.setStroke(new BasicStroke(1.5f));
            g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            g.setColor(ativo ? Tema.PAPEL : Tema.TINTA);
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            g.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            if (isFocusOwner()) {
                g.setColor(Tema.OURO);
                g.setStroke(new BasicStroke(2f));
                g.drawRect(3, 3, getWidth() - 7, getHeight() - 7);
            }
            g.dispose();
        }
    }
}
