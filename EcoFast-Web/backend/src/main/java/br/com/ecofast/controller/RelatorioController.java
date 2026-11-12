package br.com.ecofast.controller;

import br.com.ecofast.service.AuthService;
import br.com.ecofast.service.RelatorioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final AuthService auth;
    private final RelatorioService relatorios;

    public RelatorioController(AuthService auth, RelatorioService relatorios) {
        this.auth = auth;
        this.relatorios = relatorios;
    }

    @GetMapping("/painel")
    public Map<String, Object> painel(HttpSession sessao) {
        return relatorios.painel(auth.usuarioLogado(sessao));
    }
}
