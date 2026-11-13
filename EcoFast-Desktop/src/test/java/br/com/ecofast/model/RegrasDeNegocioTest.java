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

/** Testes das regras de POO, sem precisar de banco nem de tela. */
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
        assertThrows(IllegalArgumentException.class, () -> new Descarte(1L, 2L, 1L, vidro, 0.004));
        assertThrows(IllegalArgumentException.class, () -> new Descarte(1L, 2L, 1L, vidro, Double.NaN));
    }

    @Test
    void pesoVaiParaDuasCasasDecimais() {
        Descarte d = new Descarte(1L, 2L, 1L, MaterialFactory.criar("PLASTICO"), 2.2549999999);
        assertEquals(2.25, d.getPesoKg());
        assertEquals(23, d.getPontos());
    }

    @Test
    void saldoSoAumentaPorCreditar() {
        Cidadao c = new Cidadao(1L, "Teste", "t@t.com", Usuario.gerarHash("x"), "000", 10);
        c.creditar(15);
        assertEquals(25, c.getSaldoEcoCoins());
        assertThrows(IllegalArgumentException.class, () -> c.creditar(-100));
        assertThrows(IllegalArgumentException.class, () -> c.creditar(0));
        assertEquals(25, c.getSaldoEcoCoins());
    }

    @Test
    void cadaPerfilTemSuasPermissoes() {
        String hash = Usuario.gerarHash("123456");
        Usuario cidadao = new Cidadao(1L, "C", "c@c", hash, "1", 0);
        Usuario funcionario = new Funcionario(2L, "F", "f@f", hash, "F-1", 1L);
        Usuario admin = new Administrador(3L, "A", "a@a", hash, "A-1", 1L);

        assertEquals(Perfil.CIDADAO, cidadao.getPerfil());
        assertEquals(Perfil.FUNCIONARIO, funcionario.getPerfil());
        assertEquals(Perfil.ADMIN, admin.getPerfil());

        assertFalse(cidadao.podeRegistrarDescarte());
        assertTrue(funcionario.podeRegistrarDescarte());
        assertFalse(funcionario.podeVerRelatorios());
        assertTrue(admin.podeRegistrarDescarte());
        assertTrue(admin.podeVerRelatorios());
    }

    @Test
    void senhaConfereComOHash() {
        Usuario u = new Funcionario(2L, "F", "f@f", Usuario.gerarHash("123456"), "F-1", 1L);
        assertTrue(u.autenticar("123456"));
        assertFalse(u.autenticar("errada"));
        assertFalse(u.autenticar(null));
        assertEquals("8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92", Usuario.gerarHash("123456"));
    }
}
