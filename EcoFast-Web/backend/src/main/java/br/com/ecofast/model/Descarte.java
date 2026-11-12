package br.com.ecofast.model;

import br.com.ecofast.model.material.Material;

import java.time.LocalDateTime;

/**
 * Uma entrega de material reciclável. Cada formulário enviado pelo HTML vira
 * um objeto Descarte na memória antes de ir para o banco.
 */
public class Descarte {

    public static final double PESO_MAXIMO_KG = 500.0;

    private Long id;
    private Long cidadaoId;
    private Long funcionarioId;
    private Long pontoColetaId;
    private Material material;
    private double pesoKg;
    private int pontos;
    private LocalDateTime dataHora;

    public Descarte(Long cidadaoId, Long funcionarioId, Long pontoColetaId, Material material, double pesoKg) {
        if (pesoKg <= 0 || pesoKg > PESO_MAXIMO_KG) {
            throw new IllegalArgumentException("O peso deve estar entre 0 e " + PESO_MAXIMO_KG + " kg.");
        }
        this.cidadaoId = cidadaoId;
        this.funcionarioId = funcionarioId;
        this.pontoColetaId = pontoColetaId;
        this.material = material;
        this.pesoKg = pesoKg;
        this.dataHora = LocalDateTime.now();
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
