package br.com.ecofast;

import br.com.ecofast.excecao.AcessoNegadoException;
import br.com.ecofast.excecao.NaoAutenticadoException;
import br.com.ecofast.infra.Banco;
import br.com.ecofast.infra.BancoSetup;
import br.com.ecofast.infra.ConfigBanco;
import br.com.ecofast.model.Administrador;
import br.com.ecofast.model.Cidadao;
import br.com.ecofast.model.Funcionario;
import br.com.ecofast.model.Relatorio;
import br.com.ecofast.model.Usuario;
import br.com.ecofast.service.Extrato;
import br.com.ecofast.service.ReciboDescarte;
import br.com.ecofast.service.Servicos;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Testa tudo contra um SQL Server de verdade, num banco só de teste (EcoFast_Teste)
 * que é apagado no fim. Sem SQL Server configurado neste computador, os testes são pulados.
 */
class BancoSqlServerTest {

    private static final String BANCO_TESTE = "EcoFast_Teste";
    private static ConfigBanco cfg;
    private static Servicos servicos;
    private static Banco banco;

    @BeforeAll
    static void prepararBanco() {
        var salva = ConfigBanco.carregar();
        assumeTrue(salva.isPresent(), "EcoFast ainda não foi configurado neste computador");
        cfg = salva.get().comBanco(BANCO_TESTE);
        try {
            apagarBancoDeTeste();
            BancoSetup.garantir(cfg);
        } catch (Exception e) {
            assumeTrue(false, "SQL Server indisponível: " + e.getMessage());
        }
        banco = new Banco(cfg);
        servicos = Servicos.para(banco);
    }

    @AfterAll
    static void limpar() throws Exception {
        if (cfg != null) {
            apagarBancoDeTeste();
        }
    }

    private static void apagarBancoDeTeste() throws Exception {
        try (Connection c = DriverManager.getConnection(cfg.comBanco("master").url(), cfg.usuario(), cfg.senha());
             Statement st = c.createStatement()) {
            st.execute("IF DB_ID(N'" + BANCO_TESTE + "') IS NOT NULL BEGIN "
                    + "ALTER DATABASE [" + BANCO_TESTE + "] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; "
                    + "DROP DATABASE [" + BANCO_TESTE + "]; END");
        }
    }

    private int contar(String tabela) {
        return banco.consultar(c -> {
            try (Statement st = c.createStatement(); var rs = st.executeQuery("SELECT COUNT(*) FROM dbo." + tabela)) {
                rs.next();
                return rs.getInt(1);
            }
        });
    }

    private Usuario entrar(String email) {
        return servicos.auth().login(email, "123456");
    }

    @Test
    void criarOBancoDuasVezesNaoDuplicaNada() throws Exception {
        int usuarios = contar("usuario");
        int descartes = contar("descarte");
        BancoSetup.garantir(cfg);
        assertEquals(usuarios, contar("usuario"));
        assertEquals(descartes, contar("descarte"));
        assertTrue(usuarios >= 6);
        assertTrue(descartes >= 22);
    }

    @Test
    void cadaPerfilVoltaComASuaClasse() {
        assertInstanceOf(Cidadao.class, entrar("maria@email.com"));
        assertInstanceOf(Funcionario.class, entrar("funcionario@ecofast.com"));
        assertInstanceOf(Administrador.class, entrar("admin@ecofast.com"));
        assertEquals("maria@email.com", entrar("  MARIA@email.com ").getEmail());
    }

    @Test
    void senhaErradaOuEmailInexistenteNaoEntram() {
        assertThrows(NaoAutenticadoException.class, () -> servicos.auth().login("maria@email.com", "errada"));
        assertThrows(NaoAutenticadoException.class, () -> servicos.auth().login("ninguem@email.com", "123456"));
        assertThrows(NaoAutenticadoException.class, () -> servicos.auth().login("", ""));
    }

    @Test
    void entregaCreditaOSaldoEGravaNoExtrato() {
        Usuario func = entrar("funcionario@ecofast.com");
        Usuario joao = entrar("joao@email.com");
        Extrato antes = servicos.descartes().extrato(joao);

        ReciboDescarte r = servicos.descartes().registrar(func, "222.222.222-22", "ELETRONICO", 2.5);

        assertEquals(83, r.descarte().getPontos());                     // 2,5 x 25 = 62,5 -> 63, mais 20 de bônus
        assertEquals(antes.saldoEcoCoins() + 83, r.novoSaldo());
        Extrato depois = servicos.descartes().extrato(joao);
        assertEquals(antes.saldoEcoCoins() + 83, depois.saldoEcoCoins());
        assertEquals(antes.entregas().size() + 1, depois.entregas().size());
        assertEquals(r.descarte().getId(), depois.entregas().get(0).getId());
        assertEquals(2.5, depois.entregas().get(0).getPesoKg());
    }

    @Test
    void cpfSemPontuacaoTambemFunciona() {
        Usuario func = entrar("funcionario@ecofast.com");
        ReciboDescarte r = servicos.descartes().registrar(func, "33333333333", "PAPEL", 1.0);
        assertEquals("Ana Lima", r.cidadao());
        assertEquals("Ecoponto Centro", r.ecoponto());
    }

    @Test
    void cidadaoNaoRegistraEntrega() {
        Usuario maria = entrar("maria@email.com");
        assertThrows(AcessoNegadoException.class,
                () -> servicos.descartes().registrar(maria, "111.111.111-11", "METAL", 100));
        assertThrows(AcessoNegadoException.class, () -> servicos.descartes().ultimosRegistros(maria));
    }

    @Test
    void entradaInvalidaNaoMexeNoBanco() {
        Usuario func = entrar("funcionario@ecofast.com");
        int descartes = contar("descarte");
        Extrato antes = servicos.descartes().extrato(entrar("carlos@email.com"));

        assertThrows(IllegalArgumentException.class, () -> servicos.descartes().registrar(func, "999.999.999-99", "VIDRO", 1));
        assertThrows(IllegalArgumentException.class, () -> servicos.descartes().registrar(func, "444.444.444-44", "VIDRO", 999));
        assertThrows(IllegalArgumentException.class, () -> servicos.descartes().registrar(func, "444.444.444-44", "VIDRO", 0));
        assertThrows(IllegalArgumentException.class, () -> servicos.descartes().registrar(func, "444.444.444-44", "MADEIRA", 5));
        assertThrows(IllegalArgumentException.class, () -> servicos.descartes().registrar(func, "123", "VIDRO", 5));

        assertEquals(descartes, contar("descarte"));
        assertEquals(antes.saldoEcoCoins(), servicos.descartes().extrato(entrar("carlos@email.com")).saldoEcoCoins());
    }

    @Test
    void relatorioSoParaAdministrador() {
        assertThrows(AcessoNegadoException.class, () -> servicos.relatorios().painel(entrar("funcionario@ecofast.com")));
        assertThrows(AcessoNegadoException.class, () -> servicos.relatorios().painel(entrar("maria@email.com")));

        Relatorio r = servicos.relatorios().painel(entrar("admin@ecofast.com"));
        assertEquals(30, r.dias());
        assertTrue(r.resumo().entregas() >= 22);
        assertTrue(r.resumo().totalKg() > 100);
        assertTrue(r.resumo().cidadaos() >= 4);
        assertEquals(5, r.porMaterial().size());
        assertTrue(r.ranking().size() <= 5 && !r.ranking().isEmpty());
        assertTrue(r.ranking().get(0).pontos() >= r.ranking().get(r.ranking().size() - 1).pontos());
        assertTrue(r.porDia().size() > 10);
    }

    @Test
    void entregasSimultaneasNaoPerdemPontos() throws Exception {
        Usuario func = entrar("funcionario@ecofast.com");
        Usuario carlos = entrar("carlos@email.com");
        int antes = servicos.descartes().extrato(carlos).saldoEcoCoins();

        int threads = 6, porThread = 5;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Future<?>> trabalhos = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            trabalhos.add(pool.submit(() -> {
                for (int i = 0; i < porThread; i++) {
                    servicos.descartes().registrar(func, "444.444.444-44", "PAPEL", 1.0);   // 5 pontos cada
                }
            }));
        }
        for (Future<?> f : trabalhos) {
            f.get();
        }
        pool.shutdown();

        assertEquals(antes + threads * porThread * 5, servicos.descartes().extrato(carlos).saldoEcoCoins());
    }
}
