package br.com.ecofast.ui;

import br.com.ecofast.model.Cidadao;
import br.com.ecofast.model.RegistroRecente;
import br.com.ecofast.model.Usuario;
import br.com.ecofast.model.material.Material;
import br.com.ecofast.model.material.MaterialFactory;
import br.com.ecofast.service.DescarteService;
import br.com.ecofast.service.ReciboDescarte;
import br.com.ecofast.ui.componentes.Cartao;
import br.com.ecofast.ui.componentes.CartaoMaterial;
import br.com.ecofast.ui.componentes.ConteudoRolavel;
import br.com.ecofast.ui.componentes.MoedaIcone;
import br.com.ecofast.ui.componentes.TabelaDados;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.MaskFormatter;
import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/** Tela do funcionário (e do administrador): registra a entrega depois de pesar o material. */
public class PainelFuncionario extends PainelBase {

    private final Usuario operador;
    private final DescarteService servico;

    private final JFormattedTextField campoCpf;
    private final JLabel achado = new JLabel();
    private final List<CartaoMaterial> cartoes = new ArrayList<>();
    private final JSpinner peso = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 500.0, 0.1));
    private final JLabel previa = Tema.rotulo(" ", 14, Font.BOLD, Tema.MOEDA);
    private final JLabel erro = new JLabel();
    private final JButton salvar = Tema.botaoPrimario("Salvar descarte");
    private final Cartao recibo = new Cartao(new MigLayout("insets 0, wrap 2, fillx", "[grow][grow, right]", ""));
    private final TabelaDados tabela = new TabelaDados("Nenhum registro ainda.",
            new String[]{"Quando", "Cidadão", "Material", "Peso", "Pontos"}, 3, 4);

    public PainelFuncionario(Usuario operador, DescarteService servico) {
        this.operador = operador;
        this.servico = servico;

        try {
            MaskFormatter mascara = new MaskFormatter("###.###.###-##");
            mascara.setPlaceholderCharacter('_');
            campoCpf = new JFormattedTextField(mascara);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
        campoCpf.setName("cpf");
        campoCpf.setFocusLostBehavior(JFormattedTextField.PERSIST);

        ConteudoRolavel corpo = new ConteudoRolavel(new MigLayout("insets 24 28 28 28, fill",
                "[45%, fill][55%, fill]", "[top]"));
        corpo.setBackground(Tema.FUNDO);
        corpo.add(criarFormulario(), "growy, aligny top, gapright 20");
        corpo.add(criarLadoDireito(), "growy, aligny top");
        add(rolagem(corpo));
        atualizarPrevia();
    }

    private Cartao criarFormulario() {
        Cartao c = new Cartao(new MigLayout("insets 0, wrap 1, fillx, hidemode 3", "[grow, fill]", ""));
        c.add(Tema.rotulo("Registrar entrega de material", 20, Font.BOLD, Tema.VERDE_900));
        c.add(Tema.rotulo("<html><div style='width:420px'>Confira o peso na balança antes de salvar. "
                + "Os pontos são calculados pelo sistema.</div></html>", 13, Font.PLAIN, Tema.SUAVE), "gaptop 2");

        c.add(Tema.rotulo("CPF do cidadão", 13, Font.BOLD, Tema.TEXTO), "gaptop 18");
        JPanel linhaCpf = new JPanel(new MigLayout("insets 0, fillx", "[grow, fill][]", "[]"));
        linhaCpf.setOpaque(false);
        linhaCpf.add(campoCpf, "gapright 8");
        JButton buscar = Tema.botaoSecundario("Buscar");
        buscar.setName("buscar");
        buscar.addActionListener(e -> buscarCidadao());
        campoCpf.addActionListener(e -> buscarCidadao());
        linhaCpf.add(buscar);
        c.add(linhaCpf, "gaptop 4");
        achado.setVisible(false);
        achado.setOpaque(true);
        achado.setBackground(Tema.VERDE_50);
        achado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.VERDE_300), BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        c.add(achado, "gaptop 8");
        c.add(Tema.rotulo("Exemplos: 111.111.111-11 · 222.222.222-22 · 333.333.333-33", 12, Font.PLAIN, Tema.SUAVE), "gaptop 6");

        c.add(Tema.rotulo("Tipo de material", 13, Font.BOLD, Tema.TEXTO), "gaptop 18");
        JPanel grade = new JPanel(new MigLayout("insets 0, fillx", "[grow, fill][grow, fill][grow, fill][grow, fill][grow, fill]", "[]"));
        grade.setOpaque(false);
        ButtonGroup grupo = new ButtonGroup();
        for (Material m : MaterialFactory.todos()) {
            CartaoMaterial cartao = new CartaoMaterial(m);
            cartao.addActionListener(e -> atualizarPrevia());
            grupo.add(cartao);
            cartoes.add(cartao);
            grade.add(cartao, "grow, gapx 0 6");
        }
        cartoes.get(1).setSelected(true);
        c.add(grade, "gaptop 6");

        c.add(Tema.rotulo("Peso conferido na balança (kg)", 13, Font.BOLD, Tema.TEXTO), "gaptop 18");
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(peso, "0.0");
        ((DecimalFormat) editor.getFormat()).setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Fmt.ptBr()));
        peso.setEditor(editor);
        peso.setName("peso");
        peso.addChangeListener(e -> atualizarPrevia());
        c.add(peso, "w 160!, gaptop 6");
        previa.setIcon(new MoedaIcone(18));
        previa.setIconTextGap(8);
        c.add(previa, "gaptop 12");

        erro.setForeground(Tema.ERRO);
        erro.setFont(Tema.fonte(13, Font.PLAIN));
        erro.setName("erroRegistro");
        erro.setVisible(false);
        c.add(erro, "gaptop 8");

        salvar.setName("salvar");
        salvar.addActionListener(e -> salvar());
        c.add(salvar, "growx, gaptop 18");
        return c;
    }

    private JPanel criarLadoDireito() {
        JPanel lado = new JPanel(new MigLayout("insets 0, wrap 1, fillx, hidemode 3", "[grow, fill]", ""));
        lado.setOpaque(false);

        recibo.cores(Tema.VERDE_50, Tema.VERDE_500, 2f);
        recibo.setVisible(false);
        lado.add(recibo, "gapbottom 16");

        Cartao historico = new Cartao(new MigLayout("insets 0, fillx, wrap 1", "[grow, fill]", ""));
        historico.add(Tema.rotulo("Últimos registros neste ecoponto", 17, Font.BOLD, Tema.VERDE_900));
        tabela.larguras(21, 26, 21, 16, 16);
        tabela.alturaParaLinhas(6);
        historico.add(tabela, "gaptop 10");
        lado.add(historico);
        return lado;
    }

    private Material selecionado() {
        return cartoes.stream().filter(CartaoMaterial::isSelected).map(CartaoMaterial::getMaterial).findFirst().orElse(null);
    }

    private double pesoDigitado() {
        return ((Number) peso.getValue()).doubleValue();
    }

    /** Mostra, antes de salvar, quanto a entrega vai valer (cada material calcula do seu jeito). */
    private void atualizarPrevia() {
        Material m = selecionado();
        if (m == null) {
            previa.setText(" ");
            return;
        }
        int pontos = m.calcularPontos(Math.round(pesoDigitado() * 100.0) / 100.0);
        previa.setText("Esta entrega vale " + Fmt.inteiro(pontos) + " EcoCoins");
    }

    private void mostrarErro(String texto) {
        boolean tem = texto != null && !texto.isEmpty();
        erro.setText(tem ? "<html><div style='width:340px'>" + texto + "</div></html>" : "");
        erro.setVisible(tem);
    }

    private void buscarCidadao() {
        mostrarErro(null);
        String cpf = campoCpf.getText();
        Tarefa.executar(() -> servico.buscarCidadao(operador, cpf), this::mostrarAchado, msg -> {
            achado.setVisible(false);
            mostrarErro(msg);
        });
    }

    private void mostrarAchado(Cidadao c) {
        achado.setText("<html><b>" + c.getNome() + "</b> · saldo atual de <b>" + Fmt.inteiro(c.getSaldoEcoCoins())
                + "</b> EcoCoins</html>");
        achado.setFont(Tema.fonte(13, Font.PLAIN));
        achado.setForeground(Tema.VERDE_900);
        achado.setVisible(true);
    }

    private void salvar() {
        mostrarErro(null);
        Material m = selecionado();
        if (m == null) {
            mostrarErro("Escolha o tipo de material.");
            return;
        }
        String cpf = campoCpf.getText();
        double kg = pesoDigitado();
        salvar.setEnabled(false);
        Tarefa.executar(() -> servico.registrar(operador, cpf, m.getTipo(), kg), r -> {
            salvar.setEnabled(true);
            mostrarRecibo(r);
            peso.setValue(1.0);
            achado.setVisible(false);
            atualizar();
        }, msg -> {
            salvar.setEnabled(true);
            mostrarErro(msg);
        });
    }

    private void mostrarRecibo(ReciboDescarte r) {
        recibo.removeAll();
        recibo.add(Tema.rotulo("Descarte nº " + r.descarte().getId() + " registrado com sucesso!", 16, Font.BOLD,
                Tema.VERDE_900), "span 2, wrap 10");
        linha("Cidadão", r.cidadao());
        linha("Material", r.descarte().getMaterial().getNome());
        linha("Peso", Fmt.kg(r.descarte().getPesoKg()));
        linha("Ecoponto", r.ecoponto());
        linha("Data", Fmt.dataHora(r.descarte().getDataHora()));

        Cartao ganho = new Cartao(new MigLayout("insets 0, fillx", "[grow][]", "[]")).cores(Color.WHITE, Tema.BORDA, 1f);
        ganho.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        ganho.add(Tema.rotulo("Pontos ganhos  +" + Fmt.inteiro(r.descarte().getPontos()), 15, Font.BOLD, Tema.MOEDA));
        ganho.add(Tema.rotulo("Novo saldo  " + Fmt.inteiro(r.novoSaldo()), 15, Font.BOLD, Tema.MOEDA));
        recibo.add(ganho, "span 2, growx, gaptop 12");
        recibo.setVisible(true);
        recibo.revalidate();
        recibo.repaint();
    }

    private void linha(String nome, String valor) {
        recibo.add(Tema.rotulo(nome, 13, Font.PLAIN, Tema.SUAVE), "gaptop 2");
        recibo.add(Tema.rotulo(valor, 13, Font.BOLD, Tema.TEXTO), "gaptop 2");
    }

    @Override
    public void atualizar() {
        Tarefa.executar(() -> servico.ultimosRegistros(operador), registros -> {
            List<Object[]> linhas = new ArrayList<>();
            for (RegistroRecente r : registros) {
                linhas.add(new Object[]{Fmt.dataHoraCurta(r.dataHora()), r.cidadao(), Tema.nomeCurto(r.material()),
                        Fmt.kg(r.pesoKg()), "+" + Fmt.inteiro(r.pontos())});
            }
            tabela.setLinhas(linhas);
        }, this::avisar);
    }
}
