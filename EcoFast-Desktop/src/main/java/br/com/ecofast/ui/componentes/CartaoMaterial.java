package br.com.ecofast.ui.componentes;

import br.com.ecofast.model.material.Material;
import br.com.ecofast.ui.Tema;

import javax.swing.JToggleButton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/** Botão de escolha de material: bolinha da cor do material, nome e quantos pontos vale o quilo. */
public class CartaoMaterial extends JToggleButton {

    private final Material material;

    public CartaoMaterial(Material material) {
        this.material = material;
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(98, 90));
        setMinimumSize(new Dimension(64, 90));
        setToolTipText(material.getNome());
        setName("material-" + material.getTipo());
        getAccessibleContext().setAccessibleName(material.getNome());
    }

    public Material getMaterial() {
        return material;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        boolean marcado = isSelected();
        boolean sobre = getModel().isRollover();
        float espessura = marcado ? 2.5f : 1f;
        Color borda = marcado ? Tema.VERDE_700 : sobre ? Tema.VERDE_300 : Tema.BORDA;
        double meio = espessura / 2;
        RoundRectangle2D.Double forma = new RoundRectangle2D.Double(meio, meio, getWidth() - espessura, getHeight() - espessura, 18, 18);
        g.setColor(marcado ? Tema.VERDE_50 : Color.WHITE);
        g.fill(forma);
        g.setColor(borda);
        g.setStroke(new BasicStroke(espessura));
        g.draw(forma);

        g.setColor(Tema.corDoMaterial(material));
        g.fillOval(13, 13, 18, 18);
        g.setFont(Tema.fonte(14, Font.BOLD));
        g.setColor(Tema.TEXTO);
        g.drawString(Tema.nomeCurto(material), 13, 56);
        g.setFont(Tema.fonte(12, Font.PLAIN));
        g.setColor(Tema.SUAVE);
        g.drawString((int) material.getMultiplicador() + " pts/kg", 13, 74);
        g.dispose();
    }
}
