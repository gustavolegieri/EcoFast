package br.com.ecofast.model;

/**
 * Funcionário do ecoponto: é ele quem pesa o material e registra o descarte.
 * Assim o cidadão não consegue "inventar" peso para ganhar pontos (antifraude).
 */
public class Funcionario extends Usuario {

    private String matricula;
    private Long pontoColetaId;

    public Funcionario(Long id, String nome, String email, String senhaHash, String matricula, Long pontoColetaId) {
        super(id, nome, email, senhaHash);
        this.matricula = matricula;
        this.pontoColetaId = pontoColetaId;
    }

    @Override
    public String getPerfil() {
        return "FUNCIONARIO";
    }

    @Override
    public String getPaginaInicial() {
        return "/funcionario.html";
    }

    @Override
    public boolean podeRegistrarDescarte() {
        return true;
    }

    public String getMatricula() { return matricula; }
    public Long getPontoColetaId() { return pontoColetaId; }
}
