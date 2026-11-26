package br.com.ecofast.service;

import br.com.ecofast.Idioma;

/** Deixa o CPF sempre no formato 000.000.000-00, como está gravado no banco. */
public final class Cpf {

    private Cpf() {
    }

    public static String formatar(String texto) {
        String digitos = texto == null ? "" : texto.replaceAll("\\D", "");
        if (digitos.length() != 11) {
            throw new IllegalArgumentException(Idioma.t("O CPF deve ter 11 números.", "The CPF must have 11 digits."));
        }
        return digitos.substring(0, 3) + "." + digitos.substring(3, 6) + "." + digitos.substring(6, 9)
                + "-" + digitos.substring(9);
    }
}
