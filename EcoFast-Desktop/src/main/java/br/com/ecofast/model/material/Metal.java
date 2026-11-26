package br.com.ecofast.model.material;

import br.com.ecofast.Idioma;

public class Metal extends Material {

    @Override
    public String getTipo() { return "METAL"; }

    @Override
    public String getNome() { return Idioma.t("Metal / Alumínio", "Metal / Aluminum"); }

    @Override
    public double getMultiplicador() { return 15; }
}
