package br.com.ecofast.model.material;

import br.com.ecofast.Idioma;

public class Papel extends Material {

    @Override
    public String getTipo() { return "PAPEL"; }

    @Override
    public String getNome() { return Idioma.t("Papel / Papelão", "Paper / Cardboard"); }

    @Override
    public double getMultiplicador() { return 5; }
}
