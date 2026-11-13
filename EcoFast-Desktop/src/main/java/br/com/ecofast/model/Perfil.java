package br.com.ecofast.model;

/** Perfis de acesso do sistema. O banco guarda o nome da constante. */
public enum Perfil {
    CIDADAO("Cidadão"),
    FUNCIONARIO("Funcionário"),
    ADMIN("Administrador");

    private final String rotulo;

    Perfil(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
