package br.com.ecofast.model;

/**
 * Cidadão que entrega o material e acumula EcoCoins.
 *
 * ENCAPSULAMENTO: o saldo é private e NÃO tem setter. A única forma de aumentar
 * é creditar(), chamado pelo DescarteService depois que um funcionário confirma
 * o peso na balança. Na tela o cidadão só consegue ler o saldo.
 */
public class Cidadao extends Usuario {

    private final String cpf;
    private int saldoEcoCoins;

    public Cidadao(Long id, String nome, String email, String senhaHash, String cpf, int saldoEcoCoins) {
        super(id, nome, email, senhaHash);
        this.cpf = cpf;
        this.saldoEcoCoins = saldoEcoCoins;
    }

    @Override
    public Perfil getPerfil() {
        return Perfil.CIDADAO;
    }

    /** Soma os pontos de um descarte aprovado. Rejeita valores que não sejam positivos. */
    public void creditar(int pontos) {
        if (pontos <= 0) {
            throw new IllegalArgumentException("Só é possível creditar pontos positivos.");
        }
        this.saldoEcoCoins += pontos;
    }

    public String getCpf() { return cpf; }
    public int getSaldoEcoCoins() { return saldoEcoCoins; }
}
