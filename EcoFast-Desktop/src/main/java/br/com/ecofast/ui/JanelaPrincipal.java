package br.com.ecofast.ui;

import br.com.ecofast.infra.Banco;
import br.com.ecofast.infra.BancoSetup;
import br.com.ecofast.infra.ConfigBanco;
import br.com.ecofast.model.Usuario;
import br.com.ecofast.service.Servicos;
import br.com.ecofast.service.SessaoUsuario;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Optional;

/** A janela do aplicativo: alterna entre a tela de login e o painel de quem entrou. */
public final class JanelaPrincipal extends JFrame implements TelaLogin.Acoes {

    private static final String CARTA_LOGIN = "login";
    private static final String CARTA_APP = "app";

    private final CardLayout cartas = new CardLayout();
    private final JPanel raiz = new JPanel(cartas);
    private final TelaLogin login = new TelaLogin(this);
    private final JPanel palco = new JPanel(new BorderLayout());
    private final JLabel quem = Tema.rotulo(" ", 14, Font.PLAIN, Color.WHITE);
    private final SessaoUsuario sessao = new SessaoUsuario();

    /** Se vier preenchida (testes), vale no lugar do arquivo de configuração salvo. */
    private final ConfigBanco configForcada;
    private ConfigBanco config;
    private Servicos servicos;

    public JanelaPrincipal() {
        this(null);
    }

    public JanelaPrincipal(ConfigBanco configForcada) {
        super("EcoFast");
        this.configForcada = configForcada;
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setIconImages(Icone.imagens());
        setMinimumSize(new Dimension(1020, 700));
        setSize(1240, 800);
        setLocationRelativeTo(null);

        palco.setBackground(Tema.FUNDO);
        JPanel app = new JPanel(new BorderLayout());
        app.add(criarBarraSuperior(), BorderLayout.NORTH);
        app.add(palco, BorderLayout.CENTER);

        raiz.add(login, CARTA_LOGIN);
        raiz.add(app, CARTA_APP);
        setContentPane(raiz);
    }

    private JPanel criarBarraSuperior() {
        JPanel barra = new JPanel(new MigLayout("insets 10 24 10 24, fillx", "[]push[][]", "[]"));
        barra.setBackground(Tema.VERDE_700);
        JLabel marca = Tema.rotulo("EcoFast", 20, Font.BOLD, Color.WHITE);
        marca.setIcon(Icone.vetorial(30));
        marca.setIconTextGap(10);
        barra.add(marca);
        quem.setName("quemEstaLogado");
        barra.add(quem, "gapright 16");
        JButton sair = new JButton("Sair");
        sair.setName("botaoSair");
        sair.putClientProperty("FlatLaf.style",
                "background: #3E8E41; foreground: #FFFFFF; hoverBackground: #4FA053; pressedBackground: #2A6B2D;"
                        + "borderWidth: 0; focusWidth: 0; arc: 14; margin: 6,18,6,18");
        sair.setFont(Tema.fonte(13, Font.BOLD));
        sair.addActionListener(e -> sair());
        barra.add(sair);
        return barra;
    }

    public void iniciar() {
        setVisible(true);
        Optional<ConfigBanco> salva = configForcada != null ? Optional.of(configForcada) : ConfigBanco.carregar();
        if (salva.isPresent()) {
            conectar(salva.get());
        } else {
            login.setStatus(TelaLogin.Estado.ERRO, "Banco ainda não configurado");
            configurarConexao();
        }
        login.focoInicial();
    }

    private void conectar(ConfigBanco cfg) {
        login.liberar(false);
        login.setStatus(TelaLogin.Estado.CONECTANDO, "Conectando ao SQL Server…");
        Tarefa.executar(() -> {
            BancoSetup.garantir(cfg);
            return cfg;
        }, this::usar, msg -> {
            login.setStatus(TelaLogin.Estado.ERRO, "Sem conexão com o banco");
            abrirDialogo(cfg, msg);
        });
    }

    private void usar(ConfigBanco cfg) {
        this.config = cfg;
        this.servicos = Servicos.para(new Banco(cfg));
        String onde = cfg.servidor() + " · " + cfg.banco();
        login.setStatus(TelaLogin.Estado.OK, "Conectado: " + onde);
        login.liberar(true);
        login.focoInicial();
    }

    @Override
    public void configurarConexao() {
        abrirDialogo(config != null ? config : ConfigBanco.carregar().orElse(ConfigBanco.padrao()), null);
    }

    private void abrirDialogo(ConfigBanco inicial, String aviso) {
        DialogoConexao dialogo = new DialogoConexao(this, inicial, aviso);
        dialogo.setVisible(true);
        dialogo.resultado().ifPresent(this::usar);
    }

    @Override
    public void entrar(String email, String senha) {
        if (servicos == null) {
            login.mostrarErro("Ainda não há conexão com o banco de dados.");
            return;
        }
        login.mostrarErro(null);
        login.liberar(false);
        Tarefa.executar(() -> servicos.auth().login(email, senha), this::abrirApp, msg -> {
            login.liberar(true);
            login.mostrarErro(msg);
        });
    }

    private void abrirApp(Usuario usuario) {
        sessao.iniciar(usuario);
        PainelBase painel = PainelFactory.criar(usuario, servicos);
        palco.removeAll();
        palco.add(painel, BorderLayout.CENTER);
        quem.setText(usuario.getNome() + "  ·  " + usuario.getPerfil().getRotulo());
        cartas.show(raiz, CARTA_APP);
        palco.revalidate();
        painel.atualizar();
        login.limpar();
        login.liberar(true);
    }

    private void sair() {
        sessao.encerrar();
        palco.removeAll();
        cartas.show(raiz, CARTA_LOGIN);
        login.focoInicial();
    }
}
