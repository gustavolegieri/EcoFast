package br.com.ecofast.service;

import br.com.ecofast.exception.AcessoNegadoException;
import br.com.ecofast.model.Cidadao;
import br.com.ecofast.model.Descarte;
import br.com.ecofast.model.Funcionario;
import br.com.ecofast.model.Usuario;
import br.com.ecofast.model.material.Material;
import br.com.ecofast.model.material.MaterialFactory;
import br.com.ecofast.repository.DescarteRepository;
import br.com.ecofast.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Regra de negócio principal: o funcionário registra a entrega, o Java calcula
 * os pontos, credita no cidadão e grava tudo no banco numa única transação.
 */
@Service
public class DescarteService {

    private final UsuarioRepository usuarios;
    private final DescarteRepository descartes;

    public DescarteService(UsuarioRepository usuarios, DescarteRepository descartes) {
        this.usuarios = usuarios;
        this.descartes = descartes;
    }

    @Transactional
    public Descarte registrar(Usuario operador, String cpf, String tipoMaterial, double pesoKg) {
        if (!operador.podeRegistrarDescarte()) {
            throw new AcessoNegadoException("Somente funcionários do ecoponto podem registrar descartes.");
        }
        Funcionario funcionario = (Funcionario) operador;

        Cidadao cidadao = buscarCidadao(cpf);
        Material material = MaterialFactory.criar(tipoMaterial);

        // 1) O formulário vira objeto: o construtor valida o peso e já calcula os pontos
        Descarte descarte = new Descarte(cidadao.getId(), funcionario.getId(), funcionario.getPontoColetaId(),
                material, pesoKg);

        // 2) O saldo só muda por dentro do objeto (encapsulamento)
        cidadao.creditar(descarte.getPontos());

        // 3) Persistência
        descartes.salvar(descarte);
        usuarios.atualizarSaldo(cidadao);
        return descarte;
    }

    public Cidadao buscarCidadao(String cpf) {
        return usuarios.buscarCidadaoPorCpf(cpf == null ? "" : cpf.trim())
                .orElseThrow(() -> new IllegalArgumentException("Nenhum cidadão cadastrado com o CPF " + cpf + "."));
    }

    public List<Descarte> extrato(Cidadao cidadao) {
        return descartes.listarPorCidadao(cidadao.getId());
    }

    public List<Map<String, Object>> ultimosRegistros(Usuario operador) {
        if (!(operador instanceof Funcionario funcionario)) {
            throw new AcessoNegadoException("Área exclusiva dos funcionários.");
        }
        return descartes.ultimosDoPonto(funcionario.getPontoColetaId(), 8);
    }
}
