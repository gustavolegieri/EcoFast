package br.com.ecofast.service;

import br.com.ecofast.model.Descarte;

import java.util.List;

/** Saldo e histórico de um cidadão. */
public record Extrato(String nome, int saldoEcoCoins, double totalKg, List<Descarte> entregas) {
}
