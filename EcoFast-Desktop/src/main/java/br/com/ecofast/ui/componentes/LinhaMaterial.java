package br.com.ecofast.ui.componentes;

import br.com.ecofast.Idioma;
import br.com.ecofast.model.material.Eletronico;
import br.com.ecofast.model.material.Material;
import br.com.ecofast.ui.Tema;

import javax.swing.JToggleButton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Uma linha da lista de materiais: quadradinho da cor do material, nome e quantos pontos vale o quilo.
 * A linha escolhida fica em tinta, com a sombra dourada. Em linhas (e não em cartões lado a lado) o
 * nome nunca estoura, por mais longo que seja.
 */
public class LinhaMaterial extends JToggleButton {

    private static final int SOMBRA = 4;
    private static final int ALTURA = 46;

    private final Material material;
    private final String taxa;

    public LinhaMaterial(Material material) {
        this.material = material;
        String base = (int) material.getMultiplicador() + " pts/kg";
        this.taxa = material instanceof Eletronico ? base + " + " + Eletronico.BONUS_DESCARTE_SEGURO + Idioma.t(" por entrega", " per delivery") : base;
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(200, ALTURA + SOMBRA));
        setMinimumSize(new Dimension(120, ALTURA + SOMBRA));
        setName("material-" + material.getTipo());
        getAccessibleContext().setAccessibleName(material.getNome() + ", " + taxa);
    }

    public Material getMaterial() {
        return material;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        boolean marcado = isSelected();
        int largura = getWidth() - SOMBRA, altura = getHeight() - SOMBRA;

        if (marcado) {
            g.setColor(Tema.OURO);
            g.fillRect(SOMBRA, SOMBRA, largura, altura);
        }
        g.setColor(marcado ? Tema.TINTA : getModel().isRollover() ? Tema.PAPEL_2 : Tema.PAPEL);
        g.fillRect(0, 0, largura, altura);
        g.setColor(Tema.TINTA);
        g.setStroke(new BasicStroke(2f));
        g.drawRect(1, 1, largura - 2, altura - 2);

        Color texto = marcado ? Tema.PAPEL : Tema.TINTA;
        new Marcador(Tema.corDoMaterial(material), marcado ? Tema.PAPEL : Tema.TINTA, 14).paintIcon(this, g, 16, (altura - 14) / 2);
        g.setFont(Tema.fonte(16, java.awt.Font.BOLD));
        g.setColor(texto);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(Tema.nomeCurto(material), 42, (altura - fm.getHeight()) / 2 + fm.getAscent());

        g.setFont(Tema.fonteMono(13, java.awt.Font.PLAIN));
        g.setColor(marcado ? Tema.PAPEL : Tema.TINTA_SUAVE);
        FontMetrics fm2 = g.getFontMetrics();
        g.drawString(taxa, largura - 16 - fm2.stringWidth(taxa), (altura - fm2.getHeight()) / 2 + fm2.getAscent());

        if (isFocusOwner()) {
            g.setColor(marcado ? Tema.OURO : Tema.TINTA);
            g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{3f, 3f}, 0f));
            g.drawRect(5, 5, largura - 11, altura - 11);
        }
        g.dispose();
    }
}
