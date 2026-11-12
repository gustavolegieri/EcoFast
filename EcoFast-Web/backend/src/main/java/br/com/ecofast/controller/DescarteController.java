package br.com.ecofast.controller;

import br.com.ecofast.exception.AcessoNegadoException;
import br.com.ecofast.model.Cidadao;
import br.com.ecofast.model.Descarte;
import br.com.ecofast.model.Usuario;
import br.com.ecofast.model.material.MaterialFactory;
import br.com.ecofast.repository.PontoColetaRepository;
import br.com.ecofast.service.AuthService;
import br.com.ecofast.service.DescarteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DescarteController {

    /** Dados que chegam do formulário HTML do funcionário. */
    public record DescarteRequest(String cpf, String material, Double pesoKg) {
    }

    private final AuthService auth;
    private final DescarteService descartes;
    private final PontoColetaRepository pontos;

    public DescarteController(AuthService auth, DescarteService descartes, PontoColetaRepository pontos) {
        this.auth = auth;
        this.descartes = descartes;
        this.pontos = pontos;
    }

    @GetMapping("/materiais")
    public List<Map<String, Object>> materiais() {
        return MaterialFactory.todos().stream()
                .map(m -> Map.<String, Object>of("tipo", m.getTipo(), "nome", m.getNome(),
                        "multiplicador", m.getMultiplicador()))
                .toList();
    }

    /** Funcionário digita o CPF e confere de quem é antes de pesar. */
    @GetMapping("/cidadaos/{cpf}")
    public Map<String, Object> buscarCidadao(@PathVariable String cpf, HttpSession sessao) {
        exigirFuncionario(sessao);
        Cidadao c = descartes.buscarCidadao(cpf);
        return Map.of("nome", c.getNome(), "cpf", c.getCpf(), "saldoEcoCoins", c.getSaldoEcoCoins());
    }

    @PostMapping("/descartes")
    public Map<String, Object> registrar(@RequestBody DescarteRequest req, HttpSession sessao) {
        Usuario operador = auth.usuarioLogado(sessao);
        if (req.pesoKg() == null) {
            throw new IllegalArgumentException("Informe o peso em kg.");
        }
        Descarte d = descartes.registrar(operador, req.cpf(), req.material(), req.pesoKg());
        Cidadao c = descartes.buscarCidadao(req.cpf());

        Map<String, Object> recibo = itemExtrato(d);
        recibo.put("cidadao", c.getNome());
        recibo.put("novoSaldo", c.getSaldoEcoCoins());
        recibo.put("ecoponto", pontos.buscarPorId(d.getPontoColetaId()).map(p -> p.getNome()).orElse("-"));
        return recibo;
    }

    @GetMapping("/descartes/recentes")
    public List<Map<String, Object>> recentes(HttpSession sessao) {
        return descartes.ultimosRegistros(auth.usuarioLogado(sessao));
    }

    /** Extrato do cidadão logado: ele só LÊ, nunca altera. */
    @GetMapping("/extrato")
    public Map<String, Object> extrato(HttpSession sessao) {
        Usuario u = auth.usuarioLogado(sessao);
        if (!(u instanceof Cidadao c)) {
            throw new AcessoNegadoException("O extrato é exclusivo do perfil cidadão.");
        }
        List<Descarte> lista = descartes.extrato(c);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("nome", c.getNome());
        m.put("saldoEcoCoins", c.getSaldoEcoCoins());
        m.put("totalKg", lista.stream().mapToDouble(Descarte::getPesoKg).sum());
        m.put("entregas", lista.stream().map(DescarteController::itemExtrato).toList());
        return m;
    }

    private void exigirFuncionario(HttpSession sessao) {
        if (!auth.usuarioLogado(sessao).podeRegistrarDescarte()) {
            throw new AcessoNegadoException("Área exclusiva dos funcionários.");
        }
    }

    private static Map<String, Object> itemExtrato(Descarte d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.getId());
        m.put("material", d.getMaterial().getNome());
        m.put("tipo", d.getMaterial().getTipo());
        m.put("pesoKg", d.getPesoKg());
        m.put("pontos", d.getPontos());
        m.put("dataHora", d.getDataHora().toString());
        return m;
    }
}
