package br.com.ecofast.ui;

import br.com.ecofast.model.Descarte;
import br.com.ecofast.model.Regras;
import br.com.ecofast.model.Usuario;
import br.com.ecofast.service.DescarteService;
import br.com.ecofast.service.Extrato;
import br.com.ecofast.ui.componentes.Cartao;
import br.com.ecofast.ui.componentes.CartaoKpi;
import br.com.ecofast.ui.componentes.ConteudoRolavel;
import br.com.ecofast.ui.componentes.MoedaIcone;
import br.com.ecofast.ui.componentes.TabelaDados;
import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/** Tela do cidadão: ele só olha. O saldo é lido do banco e não existe botão que o altere. */
public class PainelCidadao extends PainelBase {

    private final Usuario usuario;
    private final DescarteService servico;

    private final JLabel saldo = Tema.rotulo("--", 58, Font.BOLD, Color.WHITE);
    private final CartaoKpi kpiKg = new CartaoKpi("Material reciclado");
    private final CartaoKpi kpiEntregas = new CartaoKpi("Entregas feitas");
    private final CartaoKpi kpiAterro = new CartaoKpi("Lixo que não foi para o aterro");
    private final TabelaDados tabela = new TabelaDados("Você ainda não tem entregas. Leve seu reciclável ao ecoponto!",
            new String[]{"Data", "Material", "Peso", "EcoCoins"}, 2, 3);
    private final JProgressBar progresso = new JProgressBar(0, 100);
    private final JLabel textoMeta = Tema.rotulo(" ", 13, Font.PLAIN, Tema.SUAVE);
    private final JLabel percentual = Tema.rotulo("0%", 13, Font.BOLD, Tema.VERDE_900);

    public PainelCidadao(Usuario usuario, DescarteService servico) {
        this.usuario = usuario;
        this.servico = servico;

        ConteudoRolavel corpo = new ConteudoRolavel(new MigLayout("insets 24 28 28 28, fillx, wrap 3",
                "[grow, fill, sg k][grow, fill, sg k][grow, fill, sg k]", "[]16[]16[]"));
        corpo.setBackground(Tema.FUNDO);
        corpo.add(criarHero(), "span 3, growx");
        corpo.add(kpiKg);
        corpo.add(kpiEntregas);
        corpo.add(kpiAterro);

        Cartao extrato = new Cartao(new MigLayout("insets 0, fill, wrap 1", "[grow, fill]", "[][grow, fill]"));
        JPanel titulo = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
        titulo.setOpaque(false);
        titulo.add(Tema.rotulo("Extrato de descartes", 17, Font.BOLD, Tema.VERDE_900));
        JButton atualizar = Tema.botaoLink("Atualizar");
        atualizar.setName("atualizar");
        atualizar.addActionListener(e -> atualizar());
        titulo.add(atualizar);
        extrato.add(titulo);
        tabela.larguras(27, 33, 20, 20);
        tabela.alturaParaLinhas(7);
        extrato.add(tabela, "growy, gaptop 8");
        corpo.add(extrato, "span 2, grow");

        corpo.add(criarCartaoIptu(), "grow, aligny top");
        add(rolagem(corpo));
    }

    private JPanel criarHero() {
        JPanel hero = new JPanel(new MigLayout("insets 28 34 28 34, fill", "[grow][]", "[]")) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, Tema.VERDE_700, getWidth(), getHeight(), Tema.VERDE_500));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 26, 26));
                g2.setColor(new Color(255, 255, 255, 22));
                g2.fill(new Ellipse2D.Double(getWidth() - 230, -90, 280, 280));
                g2.fill(new Ellipse2D.Double(getWidth() - 360, getHeight() - 90, 190, 190));
                g2.dispose();
            }
        };
        hero.setOpaque(false);

        JPanel esquerda = new JPanel(new MigLayout("insets 0, wrap 1", "[]", "[]2[]2[]"));
        esquerda.setOpaque(false);
        esquerda.add(Tema.rotulo("Seu saldo", 14, Font.PLAIN, new Color(255, 255, 255, 220)));
        saldo.setIcon(new MoedaIcone(52));
        saldo.setIconTextGap(14);
        esquerda.add(saldo);
        esquerda.add(Tema.rotulo("EcoCoins", 14, Font.PLAIN, new Color(255, 255, 255, 220)));
        hero.add(esquerda, "aligny center");

        JPanel nota = new JPanel(new MigLayout("insets 14 18 14 18")) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 38));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 18, 18));
                g2.dispose();
            }
        };
        nota.setOpaque(false);
        JLabel texto = Tema.rotulo("<html><div style='width:290px'>Seu saldo é protegido: ele só aumenta quando um "
                + "funcionário do ecoponto confirma, na balança, o peso da sua entrega.</div></html>", 13, Font.PLAIN, Color.WHITE);
        nota.add(texto);
        hero.add(nota, "aligny center");
        return hero;
    }

    private Cartao criarCartaoIptu() {
        Cartao c = new Cartao(new MigLayout("insets 0, wrap 1, fillx", "[grow, fill]", "[]10[]10[]4[]"));
        c.add(Tema.rotulo("Seu desconto no IPTU", 17, Font.BOLD, Tema.VERDE_900));
        c.add(Tema.rotulo("<html><div style='width:250px'>A cada " + Fmt.inteiro(Regras.META_PONTOS)
                + " EcoCoins você ganha " + Regras.DESCONTO_IPTU_PERCENTUAL + "% de desconto no IPTU.</div></html>",
                13, Font.PLAIN, Tema.SUAVE));
        progresso.putClientProperty(FlatClientProperties.PROGRESS_BAR_LARGE_HEIGHT, true);
        progresso.setForeground(Tema.VERDE_500);
        c.add(progresso);
        JPanel linha = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
        linha.setOpaque(false);
        linha.add(textoMeta);
        linha.add(percentual);
        c.add(linha);
        return c;
    }

    @Override
    public void atualizar() {
        Tarefa.executar(() -> servico.extrato(usuario), this::mostrar, this::avisar);
    }

    private void mostrar(Extrato e) {
        saldo.setText(Fmt.inteiro(e.saldoEcoCoins()));
        kpiKg.setValor(Fmt.kg(e.totalKg()));
        kpiEntregas.setValor(Fmt.inteiro(e.entregas().size()));
        kpiAterro.setValor(Fmt.kg(e.totalKg()));

        List<Object[]> linhas = new ArrayList<>();
        for (Descarte d : e.entregas()) {
            linhas.add(new Object[]{Fmt.dataHora(d.getDataHora()), d.getMaterial().getNome(), Fmt.kg(d.getPesoKg()),
                    "+" + Fmt.inteiro(d.getPontos())});
        }
        tabela.setLinhas(linhas);

        int pct = (int) Math.min(100, Math.round(100.0 * e.saldoEcoCoins() / Regras.META_PONTOS));
        progresso.setValue(pct);
        percentual.setText(pct + "%");
        int faltam = Regras.META_PONTOS - e.saldoEcoCoins();
        textoMeta.setText(faltam > 0
                ? "Faltam " + Fmt.inteiro(faltam) + " EcoCoins para o desconto."
                : "Meta atingida! O desconto já é seu.");
    }
}
