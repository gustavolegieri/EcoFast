// Tela do cidadão: somente leitura do saldo e do extrato.
const META_IPTU = 1000;

async function carregarExtrato() {
  const dados = await api("/api/extrato");

  document.getElementById("saldo").textContent = fmtNum(dados.saldoEcoCoins);
  document.getElementById("total-kg").textContent = fmtKg(dados.totalKg);
  document.getElementById("entregas").textContent = fmtNum(dados.entregas.length);
  document.getElementById("aterro").textContent = fmtKg(dados.totalKg);

  const corpo = document.getElementById("extrato");
  corpo.innerHTML = dados.entregas.length
    ? dados.entregas
        .map(
          (e) => `<tr>
            <td>${fmtData(e.dataHora)}</td>
            <td><span class="etiqueta">${escapar(e.material)}</span></td>
            <td class="num">${fmtKg(e.pesoKg)}</td>
            <td class="num moedas">+${fmtNum(e.pontos)}</td>
          </tr>`
        )
        .join("")
    : '<tr><td colspan="4" class="vazio">Você ainda não tem entregas. Leve seu reciclável ao ecoponto!</td></tr>';

  const progresso = Math.min(100, (dados.saldoEcoCoins / META_IPTU) * 100);
  document.getElementById("barra-iptu").style.width = progresso + "%";
  const faltam = META_IPTU - dados.saldoEcoCoins;
  document.getElementById("texto-iptu").textContent =
    faltam > 0
      ? `Faltam ${fmtNum(faltam)} EcoCoins para o primeiro desconto.`
      : `Você já tem ${Math.floor(dados.saldoEcoCoins / META_IPTU)} desconto(s) disponível(is)!`;
}

exigirLogin(["CIDADAO"]).then((u) => u && carregarExtrato());
