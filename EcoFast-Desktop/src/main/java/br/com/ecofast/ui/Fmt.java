package br.com.ecofast.ui;

import br.com.ecofast.Idioma;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Números e datas no formato do idioma escolhido, em qualquer computador. */
public final class Fmt {

    private Fmt() {
    }

    public static Locale locale() {
        return Idioma.atual().locale();
    }

    public static String inteiro(long n) {
        return NumberFormat.getIntegerInstance(locale()).format(n);
    }

    public static String decimal(double v) {
        NumberFormat f = NumberFormat.getNumberInstance(locale());
        f.setMinimumFractionDigits(1);
        f.setMaximumFractionDigits(1);
        return f.format(v);
    }

    public static String kg(double v) {
        return decimal(v) + " kg";
    }

    /** Só a data, como no extrato do desenho: 14/11/2026. */
    public static String data(LocalDateTime d) {
        return d.format(DateTimeFormatter.ofPattern(Idioma.t("dd/MM/yyyy", "MM/dd/yyyy"), locale()));
    }

    public static String dataHora(LocalDateTime d) {
        return d.format(DateTimeFormatter.ofPattern(Idioma.t("dd/MM/yyyy HH:mm", "MM/dd/yyyy HH:mm"), locale()));
    }

    public static String dataHoraCurta(LocalDateTime d) {
        return d.format(DateTimeFormatter.ofPattern(Idioma.t("dd/MM HH:mm", "MM/dd HH:mm"), locale()));
    }

    public static String dia(LocalDate d) {
        return d.format(DateTimeFormatter.ofPattern(Idioma.t("dd/MM", "MM/dd"), locale()));
    }
}
