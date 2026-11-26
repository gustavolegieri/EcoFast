package br.com.ecofast.model;

import br.com.ecofast.Idioma;
import br.com.ecofast.model.material.Material;

import java.time.LocalDateTime;

/**
 * Uma entrega de material reciclável. Cada formulário preenchido na tela vira
 * um objeto Descarte na memória antes de ir para o banco.
 */
public class Descarte {

    private Long id;
    private final Long cidadaoId;
    private final Long funcionarioId;
    private final Long pontoColetaId;
    private final Material material;
    private final double pesoKg;
    private final int pontos;
    private final LocalDateTime dataHora;

    /** Nova entrega: valida o peso e já calcula os pontos. */
    public Descarte(Long cidadaoId, Long funcionarioId, Long pontoColetaId, Material material, double pesoKg) {
        if (material == null) {
            throw new IllegalArgumentException(Idioma.t("Escolha o tipo de material.", "Choose the material type."));
        }
        double peso = Double.isNaN(pesoKg) ? 0 : Math.round(pesoKg * 100.0) / 100.0;
        if (peso <= 0 || peso > Regras.PESO_MAXIMO_KG) {
            throw new IllegalArgumentException(Idioma.t("O peso deve estar entre 0,01 e 500 kg.", "The weight must be between 0.01 and 500 kg."));
        }
        this.cidadaoId = cidadaoId;
        this.funcionarioId = funcionarioId;
        this.pontoColetaId = pontoColetaId;
        this.material = material;
        this.pesoKg = peso;
        this.dataHora = LocalDateTime.now().withNano(0);
        this.pontos = calcularPontos();
    }

    /** Usado ao ler um descarte já salvo no banco. */
    public Descarte(Long id, Long cidadaoId, Long funcionarioId, Long pontoColetaId, Material material,
                    double pesoKg, int pontos, LocalDateTime dataHora) {
        this.id = id;
        this.cidadaoId = cidadaoId;
        this.funcionarioId = funcionarioId;
        this.pontoColetaId = pontoColetaId;
        this.material = material;
        this.pesoKg = pesoKg;
        this.pontos = pontos;
        this.dataHora = dataHora;
    }

    /**
     * POLIMORFISMO: o Descarte não sabe qual material tem nas mãos. Ele só chama
     * material.calcularPontos() e cada subclasse (Plastico, Vidro, Eletronico...)
     * responde com a sua própria regra.
     */
    public int calcularPontos() {
        return material.calcularPontos(pesoKg);
    }

    public void setId(Long id) { this.id = id; }

    public Long getId() { return id; }
    public Long getCidadaoId() { return cidadaoId; }
    public Long getFuncionarioId() { return funcionarioId; }
    public Long getPontoColetaId() { return pontoColetaId; }
    public Material getMaterial() { return material; }
    public double getPesoKg() { return pesoKg; }
    public int getPontos() { return pontos; }
    public LocalDateTime getDataHora() { return dataHora; }
}
