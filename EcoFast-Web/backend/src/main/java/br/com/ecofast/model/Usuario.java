package br.com.ecofast.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Classe mãe (abstrata) de todos os perfis do sistema.
 *
 * HERANÇA: Cidadao, Funcionario e Administrador herdam daqui os dados de login.
 * POLIMORFISMO: cada filha responde de um jeito a getPerfil() e getPaginaInicial(),
 * por isso o mesmo login abre painéis diferentes.
 * ENCAPSULAMENTO: atributos private; a senha nunca é guardada em texto puro.
 */
public abstract class Usuario {

    private Long id;
    private String nome;
    private String email;
    private String senhaHash;

    protected Usuario(Long id, String nome, String email, String senhaHash) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
    }

    /** Nome do perfil gravado no banco (CIDADAO, FUNCIONARIO, ADMIN). */
    public abstract String getPerfil();

    /** Tela HTML que esse tipo de usuário deve abrir após o login. */
    public abstract String getPaginaInicial();

    /** Diz se o usuário pode registrar descartes no ecoponto. */
    public boolean podeRegistrarDescarte() {
        return false;
    }

    /** Diz se o usuário pode ver os relatórios gerenciais. */
    public boolean podeVerRelatorios() {
        return false;
    }

    public boolean autenticar(String senhaDigitada) {
        return senhaDigitada != null && senhaHash.equals(gerarHash(senhaDigitada));
    }

    public static String gerarHash(String senha) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(senha.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenhaHash() { return senhaHash; }
}
