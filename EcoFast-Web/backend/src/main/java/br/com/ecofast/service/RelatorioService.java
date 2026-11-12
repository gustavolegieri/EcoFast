package br.com.ecofast.service;

import br.com.ecofast.exception.AcessoNegadoException;
import br.com.ecofast.model.Usuario;
import br.com.ecofast.repository.DescarteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Dados para os gráficos do painel do administrador. */
@Service
public class RelatorioService {

    public static final int DIAS_DO_RELATORIO = 30;

    private final DescarteRepository descartes;

    public RelatorioService(DescarteRepository descartes) {
        this.descartes = descartes;
    }

    public Map<String, Object> painel(Usuario usuario) {
        if (!usuario.podeVerRelatorios()) {
            throw new AcessoNegadoException("Relatórios disponíveis apenas para administradores.");
        }
        LocalDateTime inicio = LocalDate.now().minusDays(DIAS_DO_RELATORIO - 1L).atStartOfDay();

        Map<String, Object> painel = new LinkedHashMap<>();
        painel.put("dias", DIAS_DO_RELATORIO);
        painel.put("resumo", descartes.resumoDesde(inicio));
        painel.put("porMaterial", descartes.kgPorMaterialDesde(inicio));
        painel.put("porDia", descartes.kgPorDiaDesde(inicio));
        painel.put("ranking", descartes.rankingDesde(inicio, 5));
        return painel;
    }
}
