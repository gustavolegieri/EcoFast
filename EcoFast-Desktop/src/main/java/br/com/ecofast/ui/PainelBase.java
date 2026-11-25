package br.com.ecofast.ui;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;

/**
 * Classe mãe (abstrata) de todos os painéis que aparecem depois do login.
 * Cada perfil tem o seu, mas todos sabem se "atualizar" lendo o banco de novo.
 */
public abstract class PainelBase extends JPanel {

    protected PainelBase() {
        super(new BorderLayout());
        setBackground(Tema.PAPEL);
    }

    /** Abas que ficam na barra de cima (só o administrador tem); nulo quando a tela não tem abas. */
    JComponent abas() {
        return null;
    }

    /** Lê os dados de novo e redesenha a tela. */
    public abstract void atualizar();

    /** Coloca o conteúdo numa área que rola na vertical quando a janela fica baixa. */
    protected static JScrollPane rolagem(JComponent conteudo) {
        JScrollPane r = new JScrollPane(conteudo, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        r.setBorder(null);
        r.getViewport().setBackground(Tema.PAPEL);
        r.getVerticalScrollBar().setUnitIncrement(20);
        return r;
    }

    protected void avisar(String texto) {
        JOptionPane.showMessageDialog(this, texto, "EcoFast", JOptionPane.WARNING_MESSAGE);
    }
}
