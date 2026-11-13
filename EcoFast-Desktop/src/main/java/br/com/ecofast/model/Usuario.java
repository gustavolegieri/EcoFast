package br.com.ecofast.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Classe mãe (abstrata) de todos os perfis do sistema.
 *
 * HERANÇA: Cidadao, Funcionario e Administrador herdam daqui os dados de login.
 * POLIMORFISMO: cada filha responde de um jeito a getPerfil() e às permissões,
 * e a tela escolhe o painel certo a partir disso.
 * ENCAPSULAMENTO: atributos private; a senha nunca é guardada em texto puro.
 */
public abstract class Usuario {

    private final Long id;
    private final String nome;
    private final String email;
    private final String senhaHash;

    protected Usuario(Long id, String nome, String email, String senhaHash) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
    }

    public abstract Perfil getPerfil();

    /** Diz se o usuário pode registrar descartes no ecoponto. */
    public boolean podeRegistrarDescarte() {
        return false;
    }

    /** Diz se o usuário pode ver os relatórios gerenciais. */
    public boolean podeVerRelatorios() {
        return false;
    }

    public boolean autenticar(String senhaDigitada) {
        if (senhaDigitada == null) {
            return false;
        }
        byte[] esperado = senhaHash.getBytes(StandardCharsets.UTF_8);
        byte[] calculado = gerarHash(senhaDigitada).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(esperado, calculado);
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
