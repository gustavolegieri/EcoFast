package br.com.ecofast.ui;

import br.com.ecofast.model.Descarte;
import br.com.ecofast.model.Regras;
import br.com.ecofast.model.Usuario;
import br.com.ecofast.service.DescarteService;
import br.com.ecofast.service.Extrato;
import br.com.ecofast.ui.componentes.Cartao;
import br.com.ecofast.ui.componentes.ConteudoRolavel;
import br.com.ecofast.ui.componentes.FaixaKpi;
import br.com.ecofast.ui.componentes.Regua;
import br.com.ecofast.ui.componentes.TabelaDados;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tela do cidadão: ele só olha. O saldo é lido do banco e não existe botão que o altere.
 * Medidas da página: coluna da esquerda de 400 px, da direita o resto, 32 px entre elas e 28 px entre os blocos.
 */
public class PainelCidadao extends PainelBase {

    private final Usuario usuario;
    private final DescarteService servico;

    private final Texto saldo = Tema.numero("--", 104, Tema.PAPEL);
    private final FaixaKpi indicadores = new FaixaKpi(new String[]{"Material reciclado", "Entregas feitas"},
            new String[]{"Lixo que não foi para o aterro.", null}, 44, 22);
    private final TabelaDados tabela = new TabelaDados("Você ainda não tem entregas. Leve seu reciclável ao ecoponto!",
            new String[]{"Data", "Material", "Peso", "EcoCoins"}, new int[]{0}, new int[]{2, 3});
    private final Regua regua = new Regua();
    private final Texto textoMeta = Tema.rotulo(" ", 15, Font.BOLD, Tema.TINTA).linha(22.5f);
    private final Texto percentual = Tema.mono("0%", 22, Font.PLAIN, Tema.TINTA).linha(33);
    private final Texto rodapeExtrato = Tema.mono(" ", 12, Font.PLAIN, Tema.TINTA_SUAVE).linha(18);

    public PainelCidadao(Usuario usuario, DescarteService servico) {
        this.usuario = usuario;
        this.servico = servico;

        ConteudoRolavel corpo = new ConteudoRolavel(new MigLayout("insets 36 40 48 40, gap 0, fillx",
                "[408!]24[grow, fill]", "[]"));
        corpo.setBackground(Tema.PAPEL);

        JPanel esquerda = new JPanel(new MigLayout("insets 0, gap 0, wrap 1, fillx", "[408!]", "[]20[grow, fill]"));
        esquerda.setOpaque(false);
        esquerda.add(criarPlacaDeSaldo(), "w 408!");
        esquerda.add(criarCartaoIptu(), "w 400!, growy");
        corpo.add(esquerda, "grow");

        JPanel direita = new JPanel(new MigLayout("insets 0, gap 0, wrap 1, fill", "[grow, fill]", "[]28[grow, fill]"));
        direita.setOpaque(false);
        direita.add(indicadores, "growx");
        direita.add(criarExtrato(), "grow");
        corpo.add(direita, "grow");

        add(rolagem(corpo));
    }

    private static Texto novoMono(String texto, float tamanho, float tracking, Color cor) {
        Texto t = new Texto(texto);
        t.setFont(Tema.fonteMono(tamanho, Font.PLAIN, tracking));
        t.setForeground(cor);
        return t;
    }

    /** O saldo é o foco da tela: placa em tinta, número grande e sombra dourada. */
    private Cartao criarPlacaDeSaldo() {
        Cartao c = new Cartao(new MigLayout("insets 0, gap 0, wrap 1, fillx", "[grow, fill]", ""))
                .cores(Tema.TINTA, Tema.TINTA).margens(28, 30, 30, 30).sombra(8, Tema.OURO);
        c.add(Tema.kicker("Seu saldo", Tema.OURO), "h 18!");
        saldo.setName("saldoEcoCoins");
        saldo.setFont(Tema.fonteMono(104, Font.BOLD, -0.03f));
        saldo.linha(104);
        c.add(saldo, "gaptop 10, h 104!");
        c.add(novoMono("ECOCOINS", 15, 0.1f, Tema.PAPEL).linha(22.5f), "gaptop 6, h 23!");
        JPanel nota = new JPanel(new MigLayout("insets 17 0 0 0, gap 0, fillx", "[grow, fill]", "")) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0xF3, 0xEE, 0xE2, 115));
                g2.setStroke(new java.awt.BasicStroke(1f, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_MITER, 10f,
                        new float[]{3f, 3f}, 0f));
                g2.drawLine(0, 0, getWidth(), 0);
                g2.dispose();
            }
        };
        nota.setOpaque(false);
        nota.add(new Paragrafo("Só o funcionário do ecoponto registra entregas. Aqui você consulta.",
                Tema.fonte(14, Font.PLAIN), new Color(0xF3, 0xEE, 0xE2, 217), 21, 340));
        c.add(nota, "gaptop 22, growx");
        return c;
    }

    private Cartao criarCartaoIptu() {
        Cartao c = new Cartao(new MigLayout("insets 0, gap 0, wrap 1, fillx", "[grow, fill]", ""));
        JPanel titulo = new JPanel(new MigLayout("insets 0, gap 14 0, fillx", "[grow][]", "[33!]"));
        titulo.setOpaque(false);
        titulo.add(Tema.titulo("Seu desconto no IPTU", 22, 800, Tema.TINTA), "aligny baseline");
        titulo.add(percentual, "aligny baseline");
        c.add(titulo, "h 33!");
        c.add(regua, "gaptop 14, h 18!");
        JPanel escala = new JPanel(new MigLayout("insets 0, gap 0, fillx", "[grow][]", "[18!]"));
        escala.setOpaque(false);
        escala.add(Tema.mono("0", 12, Font.PLAIN, Tema.TINTA_SUAVE).linha(18));
        escala.add(Tema.mono(Fmt.inteiro(Regras.META_PONTOS), 12, Font.PLAIN, Tema.TINTA_SUAVE).linha(18));
        c.add(escala, "gaptop 6, h 18!");
        c.add(textoMeta, "gaptop 16, h 23!");
        c.add(new Paragrafo("A cada " + Fmt.inteiro(Regras.META_PONTOS) + " EcoCoins você ganha "
                + Regras.DESCONTO_IPTU_PERCENTUAL + "% de desconto no IPTU.", Tema.fonte(14, Font.PLAIN), Tema.TINTA_SUAVE, 21, 344),
                "gaptop 4");
        return c;
    }

    private Cartao criarExtrato() {
        Cartao extrato = new Cartao(new MigLayout("insets 0, gap 0, wrap 1, fill", "[grow, fill]", "[]18[grow, fill][]"));
        JPanel titulo = new JPanel(new MigLayout("insets 0, gap 0, fillx", "[grow][]", "[32!]"));
        titulo.setOpaque(false);
        titulo.add(Tema.titulo("Extrato de descartes", 30, 800, Tema.TINTA), "aligny baseline");
        JButton atualizar = Tema.botaoLink("Atualizar");
        atualizar.setName("atualizar");
        atualizar.addActionListener(e -> atualizar());
        titulo.add(atualizar, "aligny baseline");
        extrato.add(titulo, "h 32!");
        tabela.larguras(293, 275, 226, 206);
        tabela.alturaParaLinhas(5);
        extrato.add(tabela, "growy");
        JPanel rodape = new JPanel(new MigLayout("insets 12 0 0 0, gap 0, fillx", "[grow, fill]", "")) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(Tema.TINTA);
                g.fillRect(0, 0, getWidth(), 2);
            }
        };
        rodape.setOpaque(false);
        rodape.add(rodapeExtrato);
        extrato.add(rodape, "gaptop 14, growx");
        return extrato;
    }

    @Override
    public void atualizar() {
        Tarefa.executar(() -> servico.extrato(usuario), this::mostrar, this::avisar);
    }

    void mostrar(Extrato e) {
        saldo.setText(Fmt.inteiro(e.saldoEcoCoins()));
        indicadores.setValor(0, Fmt.decimal(e.totalKg()), "kg");
        indicadores.setValor(1, Fmt.inteiro(e.entregas().size()));
        indicadores.setNota(1, e.entregas().stream().map(Descarte::getDataHora).max(LocalDateTime::compareTo)
                .map(d -> "Última em " + Fmt.dia(d.toLocalDate()) + ".").orElse(null));

        List<Object[]> linhas = new ArrayList<>();
        for (Descarte d : e.entregas()) {
            linhas.add(new Object[]{Fmt.data(d.getDataHora()),
                    new TabelaDados.CelulaMaterial(Tema.nomeCurto(d.getMaterial()), Tema.corDoMaterial(d.getMaterial())),
                    Fmt.kg(d.getPesoKg()), "+" + Fmt.inteiro(d.getPontos())});
        }
        tabela.setLinhas(linhas);
        int n = e.entregas().size();
        rodapeExtrato.setText(n == 1 ? "1 entrega no extrato." : n + " entregas no extrato.");

        int pct = (int) Math.min(100, Math.round(100.0 * e.saldoEcoCoins() / Regras.META_PONTOS));
        regua.setPercentual(pct);
        percentual.setText(pct + "%");
        int faltam = Regras.META_PONTOS - e.saldoEcoCoins();
        textoMeta.setText(faltam > 0
                ? "Faltam " + Fmt.inteiro(faltam) + " EcoCoins para o desconto."
                : "Meta atingida! O desconto já é seu.");
    }
}
