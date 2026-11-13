package br.com.ecofast.model.material;

import java.util.List;

/** Converte o código vindo da tela ou do banco no objeto Material certo. */
public final class MaterialFactory {

    private static final List<Material> TODOS = List.of(
            new Papel(), new Plastico(), new Vidro(), new Metal(), new Eletronico());

    private MaterialFactory() {
    }

    public static Material criar(String tipo) {
        for (Material m : TODOS) {
            if (m.getTipo().equalsIgnoreCase(tipo)) {
                return m;
            }
        }
        throw new IllegalArgumentException("Material desconhecido: " + tipo);
    }

    public static List<Material> todos() {
        return TODOS;
    }
}
