// Tela do funcionário: busca o cidadão, envia o formulário e mostra o recibo.
const erro = document.getElementById("erro");
const caixaCidadao = document.getElementById("cidadao");
const cpfInput = document.getElementById("cpf");

function mostrarErro(texto) {
  erro.textContent = texto;
  erro.classList.toggle("oculto", !texto);
}

// Máscara simples de CPF: 000.000.000-00
cpfInput.addEventListener("input", () => {
  const d = cpfInput.value.replace(/\D/g, "").slice(0, 11);
  cpfInput.value = d
    .replace(/^(\d{3})(\d)/, "$1.$2")
    .replace(/^(\d{3})\.(\d{3})(\d)/, "$1.$2.$3")
    .replace(/\.(\d{3})(\d)/, ".$1-$2");
  caixaCidadao.classList.add("oculto");
});

async function carregarMateriais() {
  const lista = await api("/api/materiais");
  document.getElementById("materiais").innerHTML = lista
    .map(
      (m, i) => `
      <input type="radio" name="material" id="mat-${m.tipo}" value="${m.tipo}" ${i === 0 ? "checked" : ""}>
      <label for="mat-${m.tipo}">
        <i class="fas ${ICONES_MATERIAL[m.tipo]}"></i>${escapar(m.nome)}
        <small>${m.multiplicador} EcoCoins/kg</small>
      </label>`
    )
    .join("");
}

async function buscarCidadao() {
  mostrarErro("");
  try {
    const c = await api("/api/cidadaos/" + encodeURIComponent(cpfInput.value));
    caixaCidadao.innerHTML = `<i class="fas fa-user-check"></i> <strong>${escapar(c.nome)}</strong> · saldo atual <span class="moedas"><i class="fas fa-coins"></i> ${fmtNum(c.saldoEcoCoins)}</span>`;
    caixaCidadao.classList.remove("oculto");
  } catch (e) {
    caixaCidadao.classList.add("oculto");
    mostrarErro(e.message);
  }
}

async function carregarRecentes() {
  const lista = await api("/api/descartes/recentes");
  const corpo = document.getElementById("recentes");
  if (!lista.length) {
    corpo.innerHTML = '<tr><td colspan="5" class="vazio">Nenhum registro ainda.</td></tr>';
    return;
  }
  corpo.innerHTML = lista
    .map(
      (r) => `<tr>
        <td>${fmtData(r.data_hora)}</td>
        <td>${escapar(r.cidadao)}</td>
        <td><span class="etiqueta">${NOMES_MATERIAL[r.material]}</span></td>
        <td class="num">${fmtKg(r.peso_kg)}</td>
        <td class="num">+${fmtNum(r.pontos)}</td>
      </tr>`
    )
    .join("");
}

function mostrarRecibo(r) {
  const recibo = document.getElementById("recibo");
  recibo.innerHTML = `
    <div class="topo-recibo"><i class="fas fa-circle-check"></i> Descarte nº ${r.id} registrado com sucesso!</div>
    <dl>
      <dt>Cidadão</dt><dd>${escapar(r.cidadao)}</dd>
      <dt>Material</dt><dd>${escapar(r.material)}</dd>
      <dt>Peso</dt><dd>${fmtKg(r.pesoKg)}</dd>
      <dt>Ecoponto</dt><dd>${escapar(r.ecoponto)}</dd>
      <dt>Data</dt><dd>${fmtData(r.dataHora)}</dd>
    </dl>
    <div class="ganho">
      <span>Pontos ganhos <strong class="moedas"><i class="fas fa-coins"></i> +${fmtNum(r.pontos)}</strong></span>
      <span>Novo saldo <strong class="moedas">${fmtNum(r.novoSaldo)}</strong></span>
    </div>`;
  recibo.classList.remove("oculto");
}

document.getElementById("btn-buscar").addEventListener("click", buscarCidadao);

document.getElementById("form-descarte").addEventListener("submit", async (evento) => {
  evento.preventDefault();
  mostrarErro("");
  const botao = document.getElementById("btn-salvar");
  botao.disabled = true;
  try {
    const recibo = await api("/api/descartes", {
      method: "POST",
      body: {
        cpf: cpfInput.value,
        material: document.querySelector('input[name="material"]:checked').value,
        pesoKg: parseFloat(document.getElementById("peso").value),
      },
    });
    mostrarRecibo(recibo);
    document.getElementById("peso").value = "";
    caixaCidadao.classList.add("oculto");
    carregarRecentes();
  } catch (e) {
    mostrarErro(e.message);
  } finally {
    botao.disabled = false;
  }
});

exigirLogin(["FUNCIONARIO", "ADMIN"]).then((u) => {
  if (!u) return;
  if (u.podeVerRelatorios) document.getElementById("link-admin").classList.remove("oculto");
  carregarMateriais();
  carregarRecentes();
});
