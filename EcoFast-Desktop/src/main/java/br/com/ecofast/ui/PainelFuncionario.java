package br.com.ecofast.ui;

import br.com.ecofast.model.Cidadao;
import br.com.ecofast.model.RegistroRecente;
import br.com.ecofast.model.Usuario;
import br.com.ecofast.model.material.Material;
import br.com.ecofast.model.material.MaterialFactory;
import br.com.ecofast.service.DescarteService;
import br.com.ecofast.service.ReciboDescarte;
import br.com.ecofast.ui.componentes.BordaTracejada;
import br.com.ecofast.ui.componentes.Cartao;
import br.com.ecofast.ui.componentes.ConteudoRolavel;
import br.com.ecofast.ui.componentes.LinhaMaterial;
import br.com.ecofast.ui.componentes.Marcador;
import br.com.ecofast.ui.componentes.TabelaDados;
import br.com.ecofast.ui.componentes.TicketPesagem;
import net.miginfocom.swing.MigLayout;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.MaskFormatter;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
    private final List<LinhaMaterial> linhas = new ArrayList<>();
    private final JSpinner peso = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 500.0, 0.1));
    private final JLabel previa = Tema.mono("0", 40, Font.PLAIN, Tema.TINTA);
    private final JLabel erro = new JLabel();
    private final JButton salvar = Tema.botaoPrimario("Salvar descarte");
    private final JLabel confirmacao = new JLabel();
    private final TicketPesagem ticket = new TicketPesagem();
    private final JPanel reciboBloco = new JPanel(new MigLayout("insets 0, wrap 1, fillx, hidemode 3", "[grow, fill]", ""));
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
        campoCpf.setFont(Tema.fonteMono(18, Font.PLAIN));

        ConteudoRolavel corpo = new ConteudoRolavel(new MigLayout("insets 36 40 44 40, fillx, wrap 2",
                "[46%, fill][54%, fill]", "[top]"));
        corpo.setBackground(Tema.PAPEL);
        corpo.add(criarLadoDoFormulario(), "gapright 32");
        corpo.add(criarLadoDireito());
        add(rolagem(corpo));
        atualizarPrevia();
    }

    private JPanel criarLadoDoFormulario() {
        JPanel lado = new JPanel(new MigLayout("insets 0, wrap 1, fillx", "[grow, fill]", ""));
        lado.setOpaque(false);
        lado.add(Tema.kicker("Registro de entrega", Tema.VERDE));
        lado.add(Tema.titulo("Registrar entrega", 52, Tema.TINTA), "gaptop 8");
        lado.add(Tema.rotulo("<html><div style='width:420px'>Confira o peso na balança antes de salvar. "
                + "Os pontos são calculados pelo sistema.</div></html>", 15, Font.PLAIN, Tema.TINTA_SUAVE), "gaptop 10, gapbottom 24");
        lado.add(criarFormulario());
        return lado;
    }

    private Cartao criarFormulario() {
        Cartao c = new Cartao(new MigLayout("insets 0, wrap 1, fillx, hidemode 3", "[grow, fill]", ""));

        // 01 · cidadão
        c.add(passo("01", "Cidadão"));
        c.add(Tema.nomeDoCampo("CPF do cidadão"), "gaptop 10");
        JPanel linhaCpf = new JPanel(new MigLayout("insets 0, fillx", "[grow, fill][]", "[]"));
        linhaCpf.setOpaque(false);
        linhaCpf.add(campoCpf, "gapright 10");
        JButton buscar = Tema.botaoSecundario("Buscar");
        buscar.setName("buscar");
        buscar.addActionListener(e -> buscarCidadao());
        campoCpf.addActionListener(e -> buscarCidadao());
        linhaCpf.add(buscar);
        c.add(linhaCpf, "gaptop 6");
        achado.setVisible(false);
        achado.setOpaque(true);
        achado.setBackground(Tema.PAPEL_2);
        achado.setBorder(new BordaTracejada(10));
        c.add(achado, "gaptop 10");

        // 02 · material
        c.add(passo("02", "Material"), "gaptop 30");
        JPanel lista = new JPanel(new MigLayout("insets 0, wrap 1, fillx, gap 4", "[grow, fill]", ""));
        lista.setOpaque(false);
        lista.getAccessibleContext().setAccessibleName("Tipo de material");
        ButtonGroup grupo = new ButtonGroup();
        for (Material m : MaterialFactory.todos()) {
            LinhaMaterial linha = new LinhaMaterial(m);
            linha.addActionListener(e -> atualizarPrevia());
            grupo.add(linha);
            linhas.add(linha);
            lista.add(linha);
        }
        linhas.get(1).setSelected(true);
        c.add(lista, "gaptop 10");

        // 03 · peso
        c.add(passo("03", "Peso"), "gaptop 26");
        c.add(Tema.nomeDoCampo("Peso conferido na balança"), "gaptop 10");
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(peso, "0.0#");
        ((DecimalFormat) editor.getFormat()).setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Fmt.ptBr()));
        editor.getTextField().setFont(Tema.fonteMono(26, Font.PLAIN));
        editor.getTextField().setHorizontalAlignment(SwingConstants.RIGHT);
        peso.setEditor(editor);
        peso.setName("peso");
        // O spinner só avisa depois de Enter ou troca de campo; o documento avisa a cada tecla.
        editor.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                atualizarPrevia();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                atualizarPrevia();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                atualizarPrevia();
            }
        });
        // Ponto vale como vírgula: sem isso, "2.5" seria lido como 2 kg.
        editor.getTextField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == '.') {
                    e.setKeyChar(',');
                }
            }
        });
        JPanel linhaPeso = new JPanel(new MigLayout("insets 0", "[200!][]", "[]"));
        linhaPeso.setOpaque(false);
        linhaPeso.add(peso, "growx, h 56!");
        linhaPeso.add(Tema.mono("kg", 22, Font.PLAIN, Tema.TINTA), "gapleft 12");
        c.add(linhaPeso, "gaptop 6");

        c.add(criarFaixaDaPrevia(), "gaptop 26, growx");

        erro.setForeground(Tema.ALERTA);
        erro.setFont(Tema.fonte(14, Font.BOLD));
        erro.setName("erroRegistro");
        erro.setVisible(false);
        c.add(erro, "gaptop 10");

        salvar.setName("salvar");
        salvar.addActionListener(e -> salvar());
        c.add(salvar, "growx, gaptop 18");
        return c;
    }

    /** Número de passo em verde e o nome do passo em título condensado. */
    private JPanel passo(String numero, String titulo) {
        JPanel p = new JPanel(new MigLayout("insets 0", "[]12[]", "[]"));
        p.setOpaque(false);
        p.add(Tema.mono(numero, 30, Font.PLAIN, Tema.VERDE));
        p.add(Tema.titulo(titulo, 26, Tema.TINTA), "aligny center");
        return p;
    }

    /** Faixa dourada que mostra, antes de salvar, quanto a entrega vai valer. */
    private JPanel criarFaixaDaPrevia() {
        JPanel faixa = new JPanel(new MigLayout("insets 14 18 14 18, fillx", "[grow][][]", "[]")) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Tema.OURO);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Tema.TINTA);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRect(1, 1, getWidth() - 2, getHeight() - 2);
                g2.dispose();
            }
        };
        faixa.setOpaque(false);
        faixa.add(Tema.rotulo("Esta entrega vale", 15, Font.BOLD, Tema.TINTA), "aligny center");
        previa.setName("previa");
        faixa.add(previa, "aligny center");
        faixa.add(Tema.kicker("EcoCoins", Tema.TINTA), "aligny center");
        return faixa;
    }

    private JPanel criarLadoDireito() {
        JPanel lado = new JPanel(new MigLayout("insets 6 0 0 0, wrap 1, fillx, hidemode 3", "[grow, fill]", ""));
        lado.setOpaque(false);

        reciboBloco.setOpaque(false);
        confirmacao.setFont(Tema.fonteMono(13, Font.PLAIN));
        confirmacao.setForeground(Tema.TINTA);
        confirmacao.setIcon(new Marcador(Tema.FOLHA, 11));
        confirmacao.setIconTextGap(9);
        reciboBloco.add(confirmacao, "gapbottom 4");
        reciboBloco.add(ticket, "growx");
        reciboBloco.setVisible(false);
        lado.add(reciboBloco, "gapbottom 30");

        Cartao historico = new Cartao(new MigLayout("insets 0, fillx, wrap 1", "[grow, fill]", ""));
        JPanel titulo = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
        titulo.setOpaque(false);
        titulo.add(Tema.titulo("Últimos registros", 26, Tema.TINTA));
        titulo.add(Tema.mono("neste ecoponto", 12, Font.PLAIN, Tema.TINTA_SUAVE), "aligny bottom");
        historico.add(titulo);
        tabela.larguras(21, 24, 23, 17, 15);
        tabela.alturaParaLinhas(6);
        historico.add(tabela, "gaptop 14");
        lado.add(historico);
        return lado;
    }

    private Material selecionado() {
        return linhas.stream().filter(LinhaMaterial::isSelected).map(LinhaMaterial::getMaterial).findFirst().orElse(null);
    }

    private double pesoDigitado() {
        return ((Number) peso.getValue()).doubleValue();
    }

    /** O peso que está escrito no campo agora, mesmo antes de confirmar; nulo se não for um peso válido. */
    private Double pesoNoCampo() {
        JFormattedTextField campo = ((JSpinner.DefaultEditor) peso.getEditor()).getTextField();
        try {
            return ((Number) campo.getFormatter().stringToValue(campo.getText())).doubleValue();
        } catch (ParseException e) {
            return null;
        }
    }

    /** Mostra, antes de salvar, quanto a entrega vai valer (cada material calcula do seu jeito). */
    private void atualizarPrevia() {
        Material m = selecionado();
        Double kg = pesoNoCampo();
        if (m == null || kg == null) {
            previa.setText("—");
            return;
        }
        int pontos = m.calcularPontos(Math.round(kg * 100.0) / 100.0);
        previa.setText(Fmt.inteiro(pontos));
    }

    private void mostrarErro(String texto) {
        boolean tem = texto != null && !texto.isEmpty();
        erro.setText(tem ? "<html><div style='width:420px'>" + texto + "</div></html>" : "");
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
        achado.setText("<html><b>" + c.getNome() + "</b> &nbsp;<font color='#3D5646'>saldo atual</font> &nbsp;<b>"
                + Fmt.inteiro(c.getSaldoEcoCoins()) + "</b> EcoCoins</html>");
        achado.setFont(Tema.fonte(15, Font.PLAIN));
        achado.setForeground(Tema.TINTA);
        achado.setVisible(true);
    }

    private void salvar() {
        mostrarErro(null);
        Material m = selecionado();
        if (m == null) {
            mostrarErro("Escolha o tipo de material.");
            return;
        }
        if (pesoNoCampo() == null) {
            mostrarErro("O peso deve estar entre 0,1 e 500 kg.");
            return;
        }
        try {
            peso.commitEdit();
        } catch (ParseException e) {
            throw new IllegalStateException(e);
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
        confirmacao.setText("Descarte nº " + r.descarte().getId() + " registrado com sucesso");
        ticket.mostrar(r, operador.getNome());
        reciboBloco.setVisible(true);
        reciboBloco.revalidate();
        reciboBloco.repaint();
    }

    @Override
    public void atualizar() {
        Tarefa.executar(() -> servico.ultimosRegistros(operador), registros -> {
            List<Object[]> dados = new ArrayList<>();
            for (RegistroRecente r : registros) {
                dados.add(new Object[]{Fmt.dataHoraCurta(r.dataHora()), r.cidadao(),
                        new TabelaDados.CelulaMaterial(Tema.nomeCurto(r.material()), Tema.corDoMaterial(r.material())),
                        Fmt.kg(r.pesoKg()), "+" + Fmt.inteiro(r.pontos())});
            }
            tabela.setLinhas(dados);
        }, this::avisar);
    }
}
