package br.com.ecofast.model.material;

import br.com.ecofast.Idioma;

/**
 * Lixo eletrônico vale mais e ainda ganha bônus fixo por entrega, porque é o
 * material mais perigoso de ir para o aterro. Exemplo de SOBRESCRITA (@Override)
 * do cálculo padrão herdado de Material.
 */
public class Eletronico extends Material {

    public static final int BONUS_DESCARTE_SEGURO = 20;

    @Override
    public String getTipo() { return "ELETRONICO"; }

    @Override
    public String getNome() { return Idioma.t("Lixo eletrônico", "Electronic waste"); }

    @Override
    public double getMultiplicador() { return 25; }

    @Override
    public int calcularPontos(double pesoKg) {
        return super.calcularPontos(pesoKg) + BONUS_DESCARTE_SEGURO;
    }
}
