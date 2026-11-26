package br.com.ecofast.ui;

import br.com.ecofast.Idioma;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

/**
 * Barra de cima das telas de quem já entrou: marca, (abas, só no administrador), nome com o perfil e o botão Sair.
 * Tem 64 px de altura com uma linha de 2 px em tinta embaixo, como na página.
 */
public final class BarraTopo extends JPanel {

    private final Texto nome = Tema.mono(" ", 13, Font.PLAIN, Tema.TINTA);
    private final Papel papel = new Papel();
    private final JButton sair = Tema.botaoPequeno(Idioma.t("Sair", "Sign out"));
    private final SeletorIdioma idiomas;
    private JComponent abas;

    public BarraTopo(ActionListener aoSair, Consumer<Idioma> aoTrocarIdioma) {
        super(new MigLayout("insets 0 32 2 32, gap 0, fillx", "[]push[]", "[62!]"));
        setOpaque(false);
        JLabel marca = Tema.titulo("EcoFast", 26, 900, Tema.TINTA).linha(26);
        marca.setIcon(Icone.vetorial(34));
        marca.setIconTextGap(10);
        add(marca, "aligny center");
        idiomas = new SeletorIdioma(aoTrocarIdioma);
        nome.setName("quemEstaLogado");
        sair.setName("botaoSair");
        sair.addActionListener(aoSair);
        refazer();
    }

    private void refazer() {
        // marca | folga | abas? | 32 | nome 12 perfil | 32 | Sair
        JPanel direita = new JPanel(new MigLayout("insets 0, gap 0, hidemode 3", "[]32[]12[]24[]16[]", "[grow]"));
        direita.setOpaque(false);
        if (abas != null) {
            direita.add(abas, "aligny center, growy");
        } else {
            direita.add(new JPanel(), "w 0!, h 0!");
        }
        direita.add(nome, "aligny center");
        direita.add(papel, "aligny center");
        direita.add(idiomas, "aligny center");
        direita.add(sair, "aligny center");
        if (getComponentCount() > 1) {
            remove(1);
        }
        add(direita, "grow");
        revalidate();
        repaint();
    }

    public void setUsuario(String nomeDoUsuario, String perfil) {
        nome.setText(nomeDoUsuario);
        papel.definir(perfil);
        revalidate();
    }

    /** Abas da tela (só o administrador tem), que ficam ao lado do nome. */
    public void setAbas(JComponent novas) {
        this.abas = novas;
        refazer();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(super.getPreferredSize().width, 64);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Tema.PAPEL);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Tema.TINTA);
        g.fillRect(0, getHeight() - 2, getWidth(), 2);
    }

    /** O perfil dentro de uma caixinha de linha fina, em caixa-alta espaçada. */
    private static final class Papel extends JPanel {
        private final Texto texto = Tema.mono(" ", 11, Font.PLAIN, Tema.TINTA);

        Papel() {
            super(new MigLayout("insets 2 8 2 8, gap 0", "[]", "[]"));
            setOpaque(false);
            add(texto);
            setBorder(new javax.swing.border.EmptyBorder(new Insets(1, 2, 1, 2)));
        }

        void definir(String perfil) {
            texto.setFont(Tema.fonteMono(11, Font.PLAIN, 0.06f));
            texto.setText(perfil.toUpperCase(Fmt.locale()));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setColor(Tema.TINTA);
            g.setStroke(new BasicStroke(1.5f));
            g.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
            g.dispose();
        }
    }
}
