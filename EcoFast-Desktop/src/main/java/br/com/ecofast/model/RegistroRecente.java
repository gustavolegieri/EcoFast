package br.com.ecofast.model;

import br.com.ecofast.model.material.Material;

import java.time.LocalDateTime;

/** Linha da lista "últimos registros" do ecoponto, já com o nome do cidadão. */
public record RegistroRecente(long id, String cidadao, Material material, double pesoKg, int pontos,
                              LocalDateTime dataHora) {
}
