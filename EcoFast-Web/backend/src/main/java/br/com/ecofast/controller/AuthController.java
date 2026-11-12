package br.com.ecofast.controller;

import br.com.ecofast.model.Cidadao;
import br.com.ecofast.model.Usuario;
import br.com.ecofast.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    public record LoginRequest(String email, String senha) {
    }

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest req, HttpSession sessao) {
        return dados(auth.login(req.email(), req.senha(), sessao));
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession sessao) {
        auth.logout(sessao);
        return Map.of("ok", true);
    }

    @GetMapping("/me")
    public Map<String, Object> me(HttpSession sessao) {
        return dados(auth.usuarioLogado(sessao));
    }

    /** O mesmo método monta a resposta de qualquer perfil (polimorfismo). */
    static Map<String, Object> dados(Usuario u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("nome", u.getNome());
        m.put("email", u.getEmail());
        m.put("perfil", u.getPerfil());
        m.put("paginaInicial", u.getPaginaInicial());
        m.put("podeRegistrarDescarte", u.podeRegistrarDescarte());
        m.put("podeVerRelatorios", u.podeVerRelatorios());
        if (u instanceof Cidadao c) {
            m.put("saldoEcoCoins", c.getSaldoEcoCoins());
        }
        return m;
    }
}
