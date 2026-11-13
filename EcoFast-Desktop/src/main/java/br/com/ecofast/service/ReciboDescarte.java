package br.com.ecofast.service;

import br.com.ecofast.model.Descarte;

/** O que a tela mostra depois de salvar uma entrega. */
public record ReciboDescarte(Descarte descarte, String cidadao, int novoSaldo, String ecoponto) {
}
