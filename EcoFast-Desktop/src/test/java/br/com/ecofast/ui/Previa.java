package br.com.ecofast.ui;

import br.com.ecofast.model.Cidadao;
import br.com.ecofast.model.Descarte;
import br.com.ecofast.model.material.MaterialFactory;
import br.com.ecofast.service.Extrato;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Fotografa as telas sem abrir janela e sem banco, com os mesmos dados do desenho de referência, numa escala
 * de 125% (a do monitor de quem usa). Serve para comparar o resultado, imagem contra imagem, com a página.
 * Uso: java -cp ... br.com.ecofast.ui.Previa pasta-de-saida [tela...]
 */
public final class Previa {

    private static final double ESCALA = 1.25;

    private Previa() {
    }

    public static void main(String[] args) throws Exception {
        Path saida = Path.of(args[0]);
        Files.createDirectories(saida);
        Tema.instalar();
        SwingUtilities.invokeAndWait(() -> {
            try {
                for (String tela : args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[]{"Main"}) {
                    switch (tela) {
                        case "Main" -> main(saida);
                        case "Cidadao" -> cidadao(saida);
                        default -> throw new IllegalArgumentException(tela);
                    }
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
        System.exit(0);
    }

    private static void main(Path saida) throws Exception {
        TelaLogin t = new TelaLogin(new TelaLogin.Acoes() {
            @Override
            public void entrar(String email, String senha) {
            }

            @Override
            public void configurarConexao() {
            }
        });
        t.setStatus(TelaLogin.Estado.OK, "Conectado: localhost:1433");
        t.liberar(true);
        foto(saida, "Main", t, 1280, 800);
    }

    private static Descarte d(long id, String tipo, double kg, int pontos, int dia, int mes, int ano) {
        return new Descarte(id, 1L, 1L, 1L, MaterialFactory.criar(tipo), kg, pontos, LocalDateTime.of(ano, mes, dia, 10, 0));
    }

    private static void cidadao(Path saida) throws Exception {
        Cidadao maria = new Cidadao(1L, "Maria Souza", "maria@email.com", "", "111.111.111-11", 6240);
        PainelCidadao p = new PainelCidadao(maria, null);
        p.mostrar(new Extrato("Maria Souza", 6240, 520.4, List.of(
                d(5, "PLASTICO", 12.5, 125, 14, 11, 2026), d(4, "PAPEL", 30.0, 150, 9, 11, 2026),
                d(3, "METAL", 8.2, 123, 2, 11, 2026), d(2, "ELETRONICO", 3.0, 95, 27, 10, 2026),
                d(1, "VIDRO", 15.0, 120, 20, 10, 2026))));
        foto(saida, "Cidadao", comTopo(p, "Maria Souza", "Cidadã"), 1280, 860);
    }

    private static Container comTopo(PainelBase painel, String nome, String perfil) {
        JPanel app = new JPanel(new BorderLayout());
        BarraTopo barra = new BarraTopo(e -> {
        });
        barra.setUsuario(nome, perfil);
        barra.setAbas(painel.abas());
        app.add(barra, BorderLayout.NORTH);
        app.add(painel, BorderLayout.CENTER);
        return app;
    }

    static void foto(Path saida, String nome, Container c, int largura, int altura) throws Exception {
        JFrame f = new JFrame();
        f.setUndecorated(true);
        f.setContentPane(c);
        f.getContentPane().setPreferredSize(new Dimension(largura, altura));
        f.pack();
        f.addNotify();
        f.setSize(largura, altura);
        f.getContentPane().setSize(largura, altura);
        f.validate();
        f.validate();
        BufferedImage img = new BufferedImage((int) Math.round(largura * ESCALA), (int) Math.round(altura * ESCALA),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.scale(ESCALA, ESCALA);
        f.getContentPane().paint(g);
        g.dispose();
        ImageIO.write(img, "png", saida.resolve(nome + ".png").toFile());
        System.out.println("previa " + nome);
        f.dispose();
    }
}
