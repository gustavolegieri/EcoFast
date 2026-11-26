package br.com.ecofast.model;

/** Números das regras do programa, num lugar só. */
public final class Regras {

    /** Pontos que valem uma faixa de desconto no IPTU. */
    public static final int PONTOS_POR_FAIXA = 1_000;

    /** Desconto concedido a cada faixa completada, em porcento. */
    public static final int DESCONTO_IPTU_PERCENTUAL = 10;

    /** O desconto não passa do valor inteiro do imposto. */
    public static final int DESCONTO_MAXIMO_PERCENTUAL = 100;

    /** Pontos que valem 1% de desconto: a faixa é proporcional. */
    public static final int PONTOS_POR_UM_PORCENTO = PONTOS_POR_FAIXA / DESCONTO_IPTU_PERCENTUAL;

    /** Pontos que já garantem o desconto máximo. */
    public static final int PONTOS_DESCONTO_MAXIMO =
            DESCONTO_MAXIMO_PERCENTUAL / DESCONTO_IPTU_PERCENTUAL * PONTOS_POR_FAIXA;

    /** Maior peso aceito em uma única entrega. */
    public static final double PESO_MAXIMO_KG = 500.0;

    private Regras() {
    }

    /** Desconto no IPTU que o saldo já garante, em porcento. */
    public static int descontoIptu(int saldo) {
        return Math.min(DESCONTO_MAXIMO_PERCENTUAL, saldo / PONTOS_POR_UM_PORCENTO);
    }

    /** Pontos que faltam para somar mais 1%; zero quando o desconto já é o máximo. */
    public static int pontosParaProximoPorcento(int saldo) {
        return saldo >= PONTOS_DESCONTO_MAXIMO ? 0 : PONTOS_POR_UM_PORCENTO - saldo % PONTOS_POR_UM_PORCENTO;
    }
}
