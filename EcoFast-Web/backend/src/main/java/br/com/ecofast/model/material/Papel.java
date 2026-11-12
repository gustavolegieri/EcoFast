package br.com.ecofast.model.material;

public class Papel extends Material {

    @Override
    public String getTipo() { return "PAPEL"; }

    @Override
    public String getNome() { return "Papel / Papelão"; }

    @Override
    public double getMultiplicador() { return 5; }
}
