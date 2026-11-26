package br.com.ecofast.service;

import br.com.ecofast.Idioma;
import br.com.ecofast.dao.DescarteDao;
import br.com.ecofast.excecao.AcessoNegadoException;
import br.com.ecofast.infra.Banco;
import br.com.ecofast.model.Relatorio;
import br.com.ecofast.model.Usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Números do painel do administrador. */
public class RelatorioService {

    public static final int DIAS_DO_RELATORIO = 30;

    private final Banco banco;

    public RelatorioService(Banco banco) {
        this.banco = banco;
    }

    public Relatorio painel(Usuario usuario) {
        if (!usuario.podeVerRelatorios()) {
            throw new AcessoNegadoException(Idioma.t("Relatórios disponíveis apenas para administradores.", "Reports are only available to administrators."));
        }
        LocalDateTime inicio = LocalDate.now().minusDays(DIAS_DO_RELATORIO - 1L).atStartOfDay();
        return banco.consultar(c -> {
            DescarteDao dao = new DescarteDao(c);
            return new Relatorio(DIAS_DO_RELATORIO, dao.resumoDesde(inicio), dao.kgPorMaterialDesde(inicio),
                    dao.kgPorDiaDesde(inicio), dao.rankingDesde(inicio, 5));
        });
    }
}
