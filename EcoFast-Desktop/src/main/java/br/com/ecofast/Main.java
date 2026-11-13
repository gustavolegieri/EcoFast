package br.com.ecofast;

import br.com.ecofast.ui.JanelaPrincipal;
import br.com.ecofast.ui.Tema;

import javax.swing.SwingUtilities;

/** Ponto de entrada do aplicativo: instala o visual e abre a janela. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Tema.instalar();
        SwingUtilities.invokeLater(() -> new JanelaPrincipal().iniciar());
    }
}
