package br.com.ecofast.service;

import br.com.ecofast.Idioma;
import br.com.ecofast.dao.DescarteDao;
import br.com.ecofast.dao.PontoColetaDao;
import br.com.ecofast.dao.UsuarioDao;
import br.com.ecofast.excecao.AcessoNegadoException;
import br.com.ecofast.infra.Banco;
import br.com.ecofast.model.Cidadao;
import br.com.ecofast.model.Descarte;
import br.com.ecofast.model.Funcionario;
import br.com.ecofast.model.PontoColeta;
import br.com.ecofast.model.RegistroRecente;
import br.com.ecofast.model.Usuario;
import br.com.ecofast.model.material.Material;
import br.com.ecofast.model.material.MaterialFactory;

import java.util.List;

/**
 * Regra de negócio principal: o funcionário registra a entrega, o Java calcula
 * os pontos, credita no cidadão e grava tudo no banco numa única transação.
 */
public class DescarteService {

    private static final int LIMITE_RECENTES = 6;

    private final Banco banco;

    public DescarteService(Banco banco) {
        this.banco = banco;
    }

    public ReciboDescarte registrar(Usuario operador, String cpf, String tipoMaterial, double pesoKg) {
        if (!operador.podeRegistrarDescarte()) {
            throw new AcessoNegadoException(Idioma.t("Somente funcionários do ecoponto podem registrar descartes.", "Only drop-off point staff can register deliveries."));
        }
        Funcionario funcionario = (Funcionario) operador;
        if (funcionario.getPontoColetaId() == null) {
            throw new IllegalStateException(Idioma.t("Este funcionário não está ligado a nenhum ecoponto.", "This staff member is not linked to any drop-off point."));
        }
        String cpfFormatado = Cpf.formatar(cpf);
        Material material = MaterialFactory.criar(tipoMaterial);

        return banco.emTransacao(c -> {
            UsuarioDao usuarios = new UsuarioDao(c);
            Cidadao cidadao = usuarios.buscarCidadaoPorCpfParaAtualizar(cpfFormatado)
                    .orElseThrow(() -> new IllegalArgumentException(Idioma.t("Nenhum cidadão cadastrado com o CPF ", "No citizen registered with CPF ") + cpfFormatado + "."));

            // 1) A entrega vira objeto: o construtor valida o peso e já calcula os pontos
            Descarte descarte = new Descarte(cidadao.getId(), funcionario.getId(), funcionario.getPontoColetaId(),
                    material, pesoKg);

            // 2) O saldo só muda por dentro do objeto (encapsulamento)
            cidadao.creditar(descarte.getPontos());

            // 3) Persistência, tudo junto
            new DescarteDao(c).salvar(descarte);
            usuarios.atualizarSaldo(cidadao);
            String ponto = new PontoColetaDao(c).buscarPorId(descarte.getPontoColetaId())
                    .map(PontoColeta::getNome).orElse("-");
            return new ReciboDescarte(descarte, cidadao.getNome(), cidadao.getSaldoEcoCoins(), ponto);
        });
    }

    /** Funcionário digita o CPF e confere de quem é antes de pesar. */
    public Cidadao buscarCidadao(Usuario operador, String cpf) {
        if (!operador.podeRegistrarDescarte()) {
            throw new AcessoNegadoException(Idioma.t("Área exclusiva dos funcionários.", "Staff only area."));
        }
        String cpfFormatado = Cpf.formatar(cpf);
        return banco.consultar(c -> new UsuarioDao(c).buscarCidadaoPorCpf(cpfFormatado))
                .orElseThrow(() -> new IllegalArgumentException(Idioma.t("Nenhum cidadão cadastrado com o CPF ", "No citizen registered with CPF ") + cpfFormatado + "."));
    }

    /** Extrato do cidadão logado, lido do banco na hora para o saldo vir atualizado. */
    public Extrato extrato(Usuario usuario) {
        if (!(usuario instanceof Cidadao)) {
            throw new AcessoNegadoException(Idioma.t("O extrato é exclusivo do perfil cidadão.", "The statement is only available to citizens."));
        }
        return banco.consultar(c -> {
            Cidadao atual = new UsuarioDao(c).buscarPorId(usuario.getId())
                    .map(Cidadao.class::cast)
                    .orElseThrow(() -> new IllegalStateException(Idioma.t("Usuário não encontrado.", "User not found.")));
            List<Descarte> entregas = new DescarteDao(c).listarPorCidadao(atual.getId());
            double totalKg = entregas.stream().mapToDouble(Descarte::getPesoKg).sum();
            return new Extrato(atual.getNome(), atual.getSaldoEcoCoins(), totalKg, entregas);
        });
    }

    public List<RegistroRecente> ultimosRegistros(Usuario operador) {
        if (!(operador instanceof Funcionario funcionario) || funcionario.getPontoColetaId() == null) {
            throw new AcessoNegadoException(Idioma.t("Área exclusiva dos funcionários.", "Staff only area."));
        }
        return banco.consultar(c -> new DescarteDao(c).ultimosDoPonto(funcionario.getPontoColetaId(), LIMITE_RECENTES));
    }
}
