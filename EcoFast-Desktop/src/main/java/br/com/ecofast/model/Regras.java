package br.com.ecofast.model;

/** Números das regras do programa, num lugar só. */
public final class Regras {

    /** Pontos necessários para o desconto no IPTU. */
    public static final int META_PONTOS = 10_000;

    /** Desconto concedido ao atingir a meta, em porcento. */
    public static final int DESCONTO_IPTU_PERCENTUAL = 10;

    /** Maior peso aceito em uma única entrega. */
    public static final double PESO_MAXIMO_KG = 500.0;

    private Regras() {
    }
}
