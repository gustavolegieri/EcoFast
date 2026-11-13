package br.com.ecofast.model;

/** Um ecoponto físico da cidade. */
public class PontoColeta {

    private final Long id;
    private final String nome;
    private final String endereco;

    public PontoColeta(Long id, String nome, String endereco) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEndereco() { return endereco; }
}
