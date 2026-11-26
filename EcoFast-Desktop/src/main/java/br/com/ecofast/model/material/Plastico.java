package br.com.ecofast.model.material;

import br.com.ecofast.Idioma;

public class Plastico extends Material {

    @Override
    public String getTipo() { return "PLASTICO"; }

    @Override
    public String getNome() { return Idioma.t("Plástico", "Plastic"); }

    @Override
    public double getMultiplicador() { return 10; }
}
