package br.com.ecofast.ui;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Números e datas no jeito brasileiro, em qualquer computador. */
public final class Fmt {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private Fmt() {
    }

    public static Locale ptBr() {
        return PT_BR;
    }

    public static String inteiro(long n) {
        return NumberFormat.getIntegerInstance(PT_BR).format(n);
    }

    public static String decimal(double v) {
        NumberFormat f = NumberFormat.getNumberInstance(PT_BR);
        f.setMinimumFractionDigits(1);
        f.setMaximumFractionDigits(1);
        return f.format(v);
    }

    public static String kg(double v) {
        return decimal(v) + " kg";
    }

    /** Só a data, como no extrato do desenho: 14/11/2026. */
    public static String data(LocalDateTime d) {
        return d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", PT_BR));
    }

    public static String dataHora(LocalDateTime d) {
        return d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", PT_BR));
    }

    public static String dataHoraCurta(LocalDateTime d) {
        return d.format(DateTimeFormatter.ofPattern("dd/MM HH:mm", PT_BR));
    }

    public static String dia(LocalDate d) {
        return d.format(DateTimeFormatter.ofPattern("dd/MM", PT_BR));
    }
}
