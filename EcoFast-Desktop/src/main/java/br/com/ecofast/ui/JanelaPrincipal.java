package br.com.ecofast.ui;

import br.com.ecofast.infra.Banco;
import br.com.ecofast.infra.BancoSetup;
import br.com.ecofast.infra.ConfigBanco;
import br.com.ecofast.model.Usuario;
import br.com.ecofast.service.Servicos;
import br.com.ecofast.service.SessaoUsuario;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.Optional;

/** A janela do aplicativo: alterna entre a tela de login e o painel de quem entrou. */
public final class JanelaPrincipal extends JFrame implements TelaLogin.Acoes {

    private static final String CARTA_LOGIN = "login";
    private static final String CARTA_APP = "app";

    private final CardLayout cartas = new CardLayout();
    private final JPanel raiz = new JPanel(cartas);
    private final TelaLogin login = new TelaLogin(this);
    private final JPanel palco = new JPanel(new BorderLayout());
    private final BarraTopo barra = new BarraTopo(e -> sair());
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

        palco.setBackground(Tema.PAPEL);
        JPanel app = new JPanel(new BorderLayout());
        app.add(barra, BorderLayout.NORTH);
        app.add(palco, BorderLayout.CENTER);

        raiz.add(login, CARTA_LOGIN);
        raiz.add(app, CARTA_APP);
        setContentPane(raiz);
        // área útil de 1280 x 800, o tamanho da página de referência
        raiz.setPreferredSize(new Dimension(1280, 800));
        pack();
        setLocationRelativeTo(null);
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
        barra.setUsuario(usuario.getNome(), usuario.getPerfil().getRotulo());
        barra.setAbas(painel.abas());
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
