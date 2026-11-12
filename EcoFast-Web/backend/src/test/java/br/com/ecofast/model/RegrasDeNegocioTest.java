package br.com.ecofast.model;

import br.com.ecofast.model.material.Eletronico;
import br.com.ecofast.model.material.Material;
import br.com.ecofast.model.material.MaterialFactory;
import br.com.ecofast.model.material.Papel;
import br.com.ecofast.model.material.Plastico;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Testes das regras de POO, sem precisar de banco nem servidor. */
class RegrasDeNegocioTest {

    @Test
    void mesmoMetodoRegrasDiferentes() {
        Material[] materiais = {new Papel(), new Plastico(), new Eletronico()};
        int[] esperado = {10, 20, 70}; // 2 kg de cada: 2x5, 2x10, 2x25 + 20 de bônus
        for (int i = 0; i < materiais.length; i++) {
            Descarte d = new Descarte(1L, 2L, 1L, materiais[i], 2.0);
            assertEquals(esperado[i], d.calcularPontos(), materiais[i].getNome());
        }
    }

    @Test
    void pesoInvalidoNaoViraDescarte() {
        Material vidro = MaterialFactory.criar("VIDRO");
        assertThrows(IllegalArgumentException.class, () -> new Descarte(1L, 2L, 1L, vidro, 0));
        assertThrows(IllegalArgumentException.class, () -> new Descarte(1L, 2L, 1L, vidro, -3));
        assertThrows(IllegalArgumentException.class, () -> new Descarte(1L, 2L, 1L, vidro, 999));
    }

    @Test
    void saldoSoAumentaPorCreditar() {
        Cidadao c = new Cidadao(1L, "Teste", "t@t.com", Usuario.gerarHash("x"), "000", 10);
        c.creditar(15);
        assertEquals(25, c.getSaldoEcoCoins());
        assertThrows(IllegalArgumentException.class, () -> c.creditar(-100));
        assertEquals(25, c.getSaldoEcoCoins());
    }

    @Test
    void cadaPerfilAbreUmPainel() {
        String hash = Usuario.gerarHash("123456");
        Usuario[] usuarios = {
                new Cidadao(1L, "C", "c@c", hash, "1", 0),
                new Funcionario(2L, "F", "f@f", hash, "F-1", 1L),
                new Administrador(3L, "A", "a@a", hash, "A-1", 1L)};

        assertEquals("/cidadao.html", usuarios[0].getPaginaInicial());
        assertEquals("/funcionario.html", usuarios[1].getPaginaInicial());
        assertEquals("/admin.html", usuarios[2].getPaginaInicial());

        assertFalse(usuarios[0].podeRegistrarDescarte());
        assertTrue(usuarios[1].podeRegistrarDescarte());
        assertTrue(usuarios[2].podeRegistrarDescarte());
        assertTrue(usuarios[2].podeVerRelatorios());
        assertTrue(usuarios[1].autenticar("123456"));
        assertFalse(usuarios[1].autenticar("errada"));
    }
}
