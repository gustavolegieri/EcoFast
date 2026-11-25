package br.com.ecofast.ui;

import br.com.ecofast.model.Relatorio;
import br.com.ecofast.model.Usuario;
import br.com.ecofast.service.RelatorioService;
import br.com.ecofast.ui.componentes.Cartao;
import br.com.ecofast.ui.componentes.ConteudoRolavel;
import br.com.ecofast.ui.componentes.FaixaKpi;
import br.com.ecofast.ui.componentes.GraficoBarras;
import br.com.ecofast.ui.componentes.GraficoMateriais;
import br.com.ecofast.ui.componentes.TabelaDados;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Font;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Painel do administrador: quanto foi arrecadado no mês, por dia e por material, e quem mais reciclou. */
public class PainelRelatorios extends PainelBase {

    private final Usuario usuario;
    private final RelatorioService servico;

    private final FaixaKpi indicadores = new FaixaKpi("Total arrecadado", "Entregas", "Cidadãos atendidos", "EcoCoins emitidas");
    private final GraficoBarras barras = new GraficoBarras();
    private final GraficoMateriais materiais = new GraficoMateriais();
    private final TabelaDados ranking = new TabelaDados("Sem entregas no período.",
            new String[]{"#", "Cidadão", "Kg entregues", "EcoCoins"}, 2, 3);
    private final JLabel subtitulo = Tema.rotulo(" ", 15, Font.PLAIN, Tema.TINTA_SUAVE);
    private final JLabel pico = Tema.mono(" ", 12, Font.PLAIN, Tema.TINTA_SUAVE);

    public PainelRelatorios(Usuario usuario, RelatorioService servico) {
        this.usuario = usuario;
        this.servico = servico;

        ConteudoRolavel corpo = new ConteudoRolavel(new MigLayout("insets 36 40 44 40, fillx, wrap 1",
                "[grow, fill]", "[]24[]28[]28[]"));
        corpo.setBackground(Tema.PAPEL);

        JPanel cabecalho = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
        cabecalho.setOpaque(false);
        JPanel titulos = new JPanel(new MigLayout("insets 0, wrap 1", "[]", "[]8[]10[]"));
        titulos.setOpaque(false);
        titulos.add(Tema.kicker("Relatórios", Tema.VERDE));
        titulos.add(Tema.titulo("Painel do ecoponto", 58, Tema.TINTA));
        titulos.add(subtitulo);
        cabecalho.add(titulos);
        JButton atualizar = Tema.botaoSecundario("Atualizar");
        atualizar.setName("atualizar");
        atualizar.addActionListener(e -> atualizar());
        cabecalho.add(atualizar, "aligny bottom");
        corpo.add(cabecalho);

        corpo.add(indicadores);

        Cartao cBarras = new Cartao(new MigLayout("insets 0, fill, wrap 1", "[grow, fill]", "[][grow, fill]"));
        JPanel tituloBarras = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
        tituloBarras.setOpaque(false);
        tituloBarras.add(Tema.titulo("Kg arrecadados por dia", 30, Tema.TINTA));
        tituloBarras.add(pico, "aligny bottom");
        cBarras.add(tituloBarras);
        cBarras.add(barras, "gaptop 14, h 290:290:");
        corpo.add(cBarras);

        JPanel linhaInferior = new JPanel(new MigLayout("insets 0, fill", "[grow 50, fill][grow 50, fill]", "[grow, fill]"));
        linhaInferior.setOpaque(false);
        Cartao cMateriais = new Cartao(new MigLayout("insets 0, fill, wrap 1", "[grow, fill]", "[][grow, fill]"));
        cMateriais.add(Tema.titulo("Kg por tipo de material", 30, Tema.TINTA));
        cMateriais.add(materiais, "gaptop 14, growy");
        linhaInferior.add(cMateriais, "gapright 28");
        Cartao cRanking = new Cartao(new MigLayout("insets 0, fill, wrap 1", "[grow, fill]", "[][grow, fill]"));
        cRanking.add(Tema.titulo("Quem mais reciclou", 30, Tema.TINTA));
        ranking.larguras(9, 49, 21, 21);
        ranking.alturaParaLinhas(5);
        cRanking.add(ranking, "gaptop 14");
        linhaInferior.add(cRanking);
        corpo.add(linhaInferior);

        add(rolagem(corpo));
    }

    @Override
    public void atualizar() {
        Tarefa.executar(() -> servico.painel(usuario), this::mostrar, this::avisar);
    }

    private void mostrar(Relatorio r) {
        subtitulo.setText("Volume arrecadado nos últimos " + r.dias() + " dias.");
        indicadores.setValor(0, Fmt.kg(r.resumo().totalKg()));
        indicadores.setValor(1, Fmt.inteiro(r.resumo().entregas()));
        indicadores.setValor(2, Fmt.inteiro(r.resumo().cidadaos()));
        indicadores.setValor(3, Fmt.inteiro(r.resumo().totalPontos()));

        // dias sem entrega entram como zero, para o eixo ficar contínuo
        Map<LocalDate, Double> porDia = new HashMap<>();
        r.porDia().forEach(d -> porDia.put(d.dia(), d.totalKg()));
        List<LocalDate> dias = new ArrayList<>();
        double[] valores = new double[r.dias()];
        LocalDate hoje = LocalDate.now();
        for (int i = 0; i < r.dias(); i++) {
            LocalDate dia = hoje.minusDays(r.dias() - 1L - i);
            dias.add(dia);
            valores[i] = porDia.getOrDefault(dia, 0.0);
        }
        barras.setDados(dias, valores);
        int p = barras.diaDePico();
        pico.setText(p >= 0 ? "pico: " + Fmt.kg(valores[p]) + " em " + Fmt.dia(dias.get(p)) : " ");

        List<GraficoMateriais.Fatia> fatias = new ArrayList<>();
        r.porMaterial().forEach(m -> fatias.add(new GraficoMateriais.Fatia(Tema.nomeCurto(m.material()),
                Tema.corDoMaterial(m.material()), m.totalKg())));
        materiais.setDados(fatias);

        List<Object[]> linhas = new ArrayList<>();
        int posicao = 1;
        for (Relatorio.Ranking c : r.ranking()) {
            linhas.add(new Object[]{String.format("%02d", posicao++), c.nome(), Fmt.kg(c.totalKg()), Fmt.inteiro(c.pontos())});
        }
        ranking.setLinhas(linhas);
    }
}
