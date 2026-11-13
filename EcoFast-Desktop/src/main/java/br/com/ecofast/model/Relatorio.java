package br.com.ecofast.model;

import br.com.ecofast.model.material.Material;

import java.time.LocalDate;
import java.util.List;

/** Números do painel do administrador em um período. */
public record Relatorio(int dias, Resumo resumo, List<PorMaterial> porMaterial, List<PorDia> porDia,
                        List<Ranking> ranking) {

    public record Resumo(int entregas, double totalKg, long totalPontos, int cidadaos) {
    }

    public record PorMaterial(Material material, double totalKg) {
    }

    public record PorDia(LocalDate dia, double totalKg) {
    }

    public record Ranking(String nome, double totalKg, long pontos) {
    }
}
