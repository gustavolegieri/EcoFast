package br.com.ecofast.model.material;

public class Vidro extends Material {

    @Override
    public String getTipo() { return "VIDRO"; }

    @Override
    public String getNome() { return "Vidro"; }

    @Override
    public double getMultiplicador() { return 8; }
}
