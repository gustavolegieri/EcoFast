package br.com.ecofast.ui.componentes;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;

/**
 * Painel para colocar dentro de um JScrollPane: acompanha a largura da janela
 * (nunca aparece barra horizontal) e só rola na vertical quando falta altura.
 */
public class ConteudoRolavel extends JPanel implements Scrollable {

    public ConteudoRolavel(LayoutManager layout) {
        super(layout);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visivel, int orientacao, int direcao) {
        return 24;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visivel, int orientacao, int direcao) {
        return orientacao == SwingConstants.VERTICAL ? visivel.height - 40 : visivel.width - 40;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return getParent() != null && getParent().getHeight() > getPreferredSize().height;
    }
}
