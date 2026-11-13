package br.com.ecofast.ui;

import br.com.ecofast.model.Relatorio;
import br.com.ecofast.model.Usuario;
import br.com.ecofast.service.RelatorioService;
import br.com.ecofast.ui.componentes.Cartao;
import br.com.ecofast.ui.componentes.CartaoKpi;
import br.com.ecofast.ui.componentes.ConteudoRolavel;
import br.com.ecofast.ui.componentes.GraficoBarras;
import br.com.ecofast.ui.componentes.GraficoRosca;
import br.com.ecofast.ui.componentes.TabelaDados;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
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

    private final CartaoKpi kpiKg = new CartaoKpi("Total arrecadado");
    private final CartaoKpi kpiEntregas = new CartaoKpi("Entregas");
    private final CartaoKpi kpiCidadaos = new CartaoKpi("Cidadãos atendidos");
    private final CartaoKpi kpiPontos = new CartaoKpi("EcoCoins emitidas");
    private final GraficoBarras barras = new GraficoBarras();
    private final GraficoRosca rosca = new GraficoRosca();
    private final TabelaDados ranking = new TabelaDados("Sem entregas no período.",
            new String[]{"#", "Cidadão", "Kg entregues", "EcoCoins"}, 2, 3);
    private final javax.swing.JLabel subtitulo = Tema.rotulo(" ", 14, Font.PLAIN, Tema.SUAVE);

    public PainelRelatorios(Usuario usuario, RelatorioService servico) {
        this.usuario = usuario;
        this.servico = servico;

        ConteudoRolavel corpo = new ConteudoRolavel(new MigLayout("insets 24 28 28 28, fillx, wrap 1",
                "[grow, fill]", "[]14[]16[]16[]"));
        corpo.setBackground(Tema.FUNDO);

        JPanel cabecalho = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
        cabecalho.setOpaque(false);
        JPanel titulos = new JPanel(new MigLayout("insets 0, wrap 1", "[]", "[]2[]"));
        titulos.setOpaque(false);
        titulos.add(Tema.rotulo("Painel do ecoponto", 22, Font.BOLD, Tema.VERDE_900));
        titulos.add(subtitulo);
        cabecalho.add(titulos);
        JButton atualizar = Tema.botaoSecundario("Atualizar");
        atualizar.setName("atualizar");
        atualizar.addActionListener(e -> atualizar());
        cabecalho.add(atualizar, "aligny center");
        corpo.add(cabecalho);

        JPanel linhaKpi = new JPanel(new MigLayout("insets 0, fillx", "[grow, fill, sg k][grow, fill, sg k][grow, fill, sg k][grow, fill, sg k]", "[]"));
        linhaKpi.setOpaque(false);
        linhaKpi.add(kpiKg, "gapright 16");
        linhaKpi.add(kpiEntregas, "gapright 16");
        linhaKpi.add(kpiCidadaos, "gapright 16");
        linhaKpi.add(kpiPontos);
        corpo.add(linhaKpi);

        JPanel linhaGraficos = new JPanel(new MigLayout("insets 0, fill", "[grow 58, fill][grow 42, fill]", "[grow, fill]"));
        linhaGraficos.setOpaque(false);
        Cartao cBarras = new Cartao(new MigLayout("insets 0, fill, wrap 1", "[grow, fill]", "[][grow, fill]"));
        cBarras.add(Tema.rotulo("Kg arrecadados por dia", 17, Font.BOLD, Tema.VERDE_900));
        cBarras.add(barras, "gaptop 6, h 280:280:");
        linhaGraficos.add(cBarras, "gapright 16");
        Cartao cRosca = new Cartao(new MigLayout("insets 0, fill, wrap 1", "[grow, fill]", "[][grow, fill]"));
        cRosca.add(Tema.rotulo("Kg por tipo de material", 17, Font.BOLD, Tema.VERDE_900));
        cRosca.add(rosca, "gaptop 6, h 280:280:");
        linhaGraficos.add(cRosca);
        corpo.add(linhaGraficos);

        Cartao cRanking = new Cartao(new MigLayout("insets 0, fill, wrap 1", "[grow, fill]", "[][grow, fill]"));
        cRanking.add(Tema.rotulo("Cidadãos que mais reciclaram", 17, Font.BOLD, Tema.VERDE_900));
        ranking.larguras(8, 52, 20, 20);
        ranking.alturaParaLinhas(5);
        cRanking.add(ranking, "gaptop 8");
        corpo.add(cRanking);

        add(rolagem(corpo));
    }

    @Override
    public void atualizar() {
        Tarefa.executar(() -> servico.painel(usuario), this::mostrar, this::avisar);
    }

    private void mostrar(Relatorio r) {
        subtitulo.setText("Volume arrecadado nos últimos " + r.dias() + " dias.");
        kpiKg.setValor(Fmt.kg(r.resumo().totalKg()));
        kpiEntregas.setValor(Fmt.inteiro(r.resumo().entregas()));
        kpiCidadaos.setValor(Fmt.inteiro(r.resumo().cidadaos()));
        kpiPontos.setValor(Fmt.inteiro(r.resumo().totalPontos()));

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

        List<GraficoRosca.Fatia> fatias = new ArrayList<>();
        r.porMaterial().forEach(m -> fatias.add(new GraficoRosca.Fatia(Tema.nomeCurto(m.material()),
                Tema.corDoMaterial(m.material()), m.totalKg())));
        rosca.setDados(fatias);

        List<Object[]> linhas = new ArrayList<>();
        int posicao = 1;
        for (Relatorio.Ranking c : r.ranking()) {
            linhas.add(new Object[]{posicao++ + "º", c.nome(), Fmt.kg(c.totalKg()), Fmt.inteiro(c.pontos())});
        }
        ranking.setLinhas(linhas);
    }
}
