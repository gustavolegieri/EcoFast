package br.com.ecofast.ui.componentes;

import br.com.ecofast.ui.Tema;
import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import java.awt.Font;

/** Cartão com um número grande e o nome dele em cima. */
public class CartaoKpi extends Cartao {

    private final JLabel valor = Tema.rotulo("--", 28, Font.BOLD, Tema.VERDE_900);

    public CartaoKpi(String titulo) {
        super(new MigLayout("insets 0, wrap 1, fillx", "[grow]", "[]4[]"));
        add(Tema.rotulo(titulo, 13, Font.PLAIN, Tema.SUAVE));
        add(valor);
    }

    public void setValor(String texto) {
        valor.setText(texto);
    }
}
