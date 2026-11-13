package br.com.ecofast.ui;

import br.com.ecofast.model.Usuario;
import br.com.ecofast.service.Servicos;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.JTabbedPane;

/**
 * Tela do administrador: duas abas. Os relatórios são só dele; o registro de
 * descarte é a mesma tela do funcionário, porque o Administrador "é um" Funcionario.
 */
public class PainelAdmin extends PainelBase {

    private final JTabbedPane abas = new JTabbedPane();

    public PainelAdmin(Usuario usuario, Servicos servicos) {
        abas.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);
        abas.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_INSETS, new java.awt.Insets(0, 22, 0, 22));
        abas.setName("abas");
        abas.addTab("Relatórios", new PainelRelatorios(usuario, servicos.relatorios()));
        abas.addTab("Registrar descarte", new PainelFuncionario(usuario, servicos.descartes()));
        abas.addChangeListener(e -> atualizar());
        add(abas);
    }

    @Override
    public void atualizar() {
        ((PainelBase) abas.getSelectedComponent()).atualizar();
    }
}
