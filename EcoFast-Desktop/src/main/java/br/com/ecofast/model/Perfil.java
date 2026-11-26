package br.com.ecofast.model;

import br.com.ecofast.Idioma;

/** Perfis de acesso do sistema. O banco guarda o nome da constante. */
public enum Perfil {
    CIDADAO,
    FUNCIONARIO,
    ADMIN;

    public String getRotulo() {
        return switch (this) {
            case CIDADAO -> Idioma.t("Cidadão", "Citizen");
            case FUNCIONARIO -> Idioma.t("Funcionário", "Staff");
            case ADMIN -> Idioma.t("Administrador", "Administrator");
        };
    }
}
