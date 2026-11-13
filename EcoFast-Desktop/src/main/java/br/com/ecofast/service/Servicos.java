package br.com.ecofast.service;

import br.com.ecofast.infra.Banco;

/** Os serviços do aplicativo, todos ligados ao mesmo banco. */
public record Servicos(AuthService auth, DescarteService descartes, RelatorioService relatorios) {

    public static Servicos para(Banco banco) {
        return new Servicos(new AuthService(banco), new DescarteService(banco), new RelatorioService(banco));
    }
}
