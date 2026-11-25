package br.com.ecofast.ui;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * O logo do EcoFast (uma folha num quadrado verde), desenhado em código. Serve para a
 * janela, para o cabeçalho e, rodando esta classe, para gerar o icon.ico do .exe.
 */
public final class Icone {

    private static final int[] TAMANHOS_ICO = {16, 24, 32, 48, 64, 128, 256};

    private Icone() {
    }

    /** O logo na versão escura (quadrado verde, folha branca): topo das telas, janela e ícone do .exe. */
    public static void desenhar(Graphics2D g0, double lado) {
        desenhar(g0, lado, false);
    }

    /**
     * Desenha o logo num quadrado de lado "lado", a partir do ponto (0,0), com as medidas do SVG da página
     * (grade de 34). Na versão clara (sobre fundo verde) o quadrado é branco e a folha é verde.
     */
    public static void desenhar(Graphics2D g0, double lado, boolean claro) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.scale(lado / 34.0, lado / 34.0);

        g.setColor(claro ? Tema.BRANCO : Tema.VERDE);
        g.fill(new Rectangle2D.Double(1, 1, 32, 32));
        g.setColor(Tema.TINTA);
        g.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g.draw(new Rectangle2D.Double(1, 1, 32, 32));

        Path2D folha = new Path2D.Double();
        folha.moveTo(9, 24);
        folha.curveTo(9, 16, 14, 11, 25, 10);
        folha.curveTo(24, 19, 20, 24, 12, 24);
        folha.closePath();
        g.setColor(claro ? Tema.VERDE : Tema.BRANCO);
        g.fill(folha);

        g.setColor(claro ? Tema.BRANCO : Tema.VERDE);
        g.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g.draw(new Line2D.Double(9, 25, 18, 16));
        g.dispose();
    }

    public static BufferedImage logo(int lado) {
        BufferedImage img = new BufferedImage(lado, lado, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        desenhar(g, lado);
        g.dispose();
        return img;
    }

    /** Ícone vetorial: fica nítido em qualquer zoom do Windows. */
    public static Icon vetorial(int lado) {
        return vetorial(lado, false);
    }

    public static Icon vetorial(int lado, boolean claro) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.translate(x, y);
                desenhar(g2, lado, claro);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return lado;
            }

            @Override
            public int getIconHeight() {
                return lado;
            }
        };
    }

    /** Vários tamanhos para a janela escolher o melhor (barra de tarefas, Alt+Tab, título). */
    public static List<Image> imagens() {
        List<Image> lista = new ArrayList<>();
        for (int t : new int[]{16, 32, 48, 64, 128, 256}) {
            lista.add(logo(t));
        }
        return lista;
    }

    /** Gera um .ico com os tamanhos padrão do Windows (cada um guardado como PNG). */
    public static void escreverIco(Path destino) throws IOException {
        List<byte[]> pngs = new ArrayList<>();
        for (int t : TAMANHOS_ICO) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(logo(t), "png", baos);
            pngs.add(baos.toByteArray());
        }
        int n = pngs.size();
        int total = 6 + 16 * n;
        for (byte[] p : pngs) {
            total += p.length;
        }
        ByteBuffer b = ByteBuffer.allocate(total).order(ByteOrder.LITTLE_ENDIAN);
        b.putShort((short) 0).putShort((short) 1).putShort((short) n);
        int deslocamento = 6 + 16 * n;
        for (int i = 0; i < n; i++) {
            int t = TAMANHOS_ICO[i];
            b.put((byte) (t >= 256 ? 0 : t)).put((byte) (t >= 256 ? 0 : t)).put((byte) 0).put((byte) 0);
            b.putShort((short) 1).putShort((short) 32);
            b.putInt(pngs.get(i).length).putInt(deslocamento);
            deslocamento += pngs.get(i).length;
        }
        for (byte[] p : pngs) {
            b.put(p);
        }
        Files.createDirectories(destino.toAbsolutePath().getParent());
        Files.write(destino, b.array());
    }

    /** Uso: java -cp target/classes br.com.ecofast.ui.Icone caminho/icon.ico */
    public static void main(String[] args) throws IOException {
        escreverIco(Path.of(args.length > 0 ? args[0] : "icon.ico"));
    }
}
