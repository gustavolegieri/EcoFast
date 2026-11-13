package br.com.ecofast.model.material;

public class Metal extends Material {

    @Override
    public String getTipo() { return "METAL"; }

    @Override
    public String getNome() { return "Metal / Alumínio"; }

    @Override
    public double getMultiplicador() { return 15; }
}
