package br.com.ecofast;

import java.util.Locale;
import java.util.prefs.Preferences;

/**
 * Idioma do aplicativo. Cada texto visível é escrito nos dois idiomas, lado a lado, com {@link #t},
 * então não tem como esquecer uma tradução. A escolha fica guardada para a próxima abertura.
 */
public enum Idioma {
    PT("pt-BR"),
    EN("en-US");

    private static final String CHAVE = "idioma";
    private static volatile Idioma atual = PT;

    private final Locale locale;

    Idioma(String etiqueta) {
        this.locale = Locale.forLanguageTag(etiqueta);
    }

    /** Formato de números e datas deste idioma. */
    public Locale locale() {
        return locale;
    }

    public static Idioma atual() {
        return atual;
    }

    /** Troca o idioma em uso. As telas já abertas precisam ser montadas de novo. */
    public static void definir(Idioma novo) {
        atual = novo;
    }

    /** Escolhe o texto no idioma em uso. */
    public static String t(String portugues, String ingles) {
        return atual == EN ? ingles : portugues;
    }

    /** Lê a escolha guardada neste computador; sem escolha, fica em português. */
    public static void carregar() {
        String salvo = Preferences.userNodeForPackage(Idioma.class).get(CHAVE, PT.name());
        atual = EN.name().equals(salvo) ? EN : PT;
    }

    public static void guardar() {
        Preferences.userNodeForPackage(Idioma.class).put(CHAVE, atual.name());
    }
}
