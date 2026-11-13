package br.com.ecofast.ui;

import br.com.ecofast.model.Perfil;
import br.com.ecofast.model.Usuario;
import br.com.ecofast.service.Servicos;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Escolhe o painel certo para o perfil de quem entrou. A tela de login não sabe
 * qual painel vai abrir: ela só entrega o Usuario, e a fábrica decide.
 */
public final class PainelFactory {

    private static final Map<Perfil, BiFunction<Usuario, Servicos, PainelBase>> FABRICAS = new EnumMap<>(Perfil.class);

    static {
        FABRICAS.put(Perfil.CIDADAO, (u, s) -> new PainelCidadao(u, s.descartes()));
        FABRICAS.put(Perfil.FUNCIONARIO, (u, s) -> new PainelFuncionario(u, s.descartes()));
        FABRICAS.put(Perfil.ADMIN, PainelAdmin::new);
    }

    private PainelFactory() {
    }

    public static PainelBase criar(Usuario usuario, Servicos servicos) {
        return FABRICAS.get(usuario.getPerfil()).apply(usuario, servicos);
    }
}
