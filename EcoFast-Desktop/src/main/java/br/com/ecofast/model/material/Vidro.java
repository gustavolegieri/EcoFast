package br.com.ecofast.model.material;

import br.com.ecofast.Idioma;

public class Vidro extends Material {

    @Override
    public String getTipo() { return "VIDRO"; }

    @Override
    public String getNome() { return Idioma.t("Vidro", "Glass"); }

    @Override
    public double getMultiplicador() { return 8; }
}
