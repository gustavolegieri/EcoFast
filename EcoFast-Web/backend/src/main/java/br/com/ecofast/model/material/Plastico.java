package br.com.ecofast.model.material;

public class Plastico extends Material {

    @Override
    public String getTipo() { return "PLASTICO"; }

    @Override
    public String getNome() { return "Plástico"; }

    @Override
    public double getMultiplicador() { return 10; }
}
