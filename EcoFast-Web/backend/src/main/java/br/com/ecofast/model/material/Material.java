package br.com.ecofast.model.material;

/**
 * Classe abstrata de material reciclável.
 *
 * POLIMORFISMO: todas as subclasses têm o MESMO método calcularPontos(peso),
 * mas cada uma define seu multiplicador e pode sobrescrever a regra.
 */
public abstract class Material {

    /** Código gravado no banco (PLASTICO, VIDRO...). */
    public abstract String getTipo();

    /** Nome amigável exibido nas telas. */
    public abstract String getNome();

    /** Quantas EcoCoins cada 1 kg desse material vale. */
    public abstract double getMultiplicador();

    public int calcularPontos(double pesoKg) {
        return (int) Math.round(pesoKg * getMultiplicador());
    }
}
