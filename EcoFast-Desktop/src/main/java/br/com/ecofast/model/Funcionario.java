package br.com.ecofast.model;

/**
 * Funcionário do ecoponto: é ele quem pesa o material e registra o descarte.
 * Assim o cidadão não consegue "inventar" peso para ganhar pontos (antifraude).
 */
public class Funcionario extends Usuario {

    private final String matricula;
    private final Long pontoColetaId;

    public Funcionario(Long id, String nome, String email, String senhaHash, String matricula, Long pontoColetaId) {
        super(id, nome, email, senhaHash);
        this.matricula = matricula;
        this.pontoColetaId = pontoColetaId;
    }

    @Override
    public Perfil getPerfil() {
        return Perfil.FUNCIONARIO;
    }

    @Override
    public boolean podeRegistrarDescarte() {
        return true;
    }

    public String getMatricula() { return matricula; }
    public Long getPontoColetaId() { return pontoColetaId; }
}
