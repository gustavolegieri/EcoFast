// Painel do administrador: consome /api/relatorios/painel e desenha os gráficos.
const CORES_MATERIAL = {
  PAPEL: "#1e88e5",
  PLASTICO: "#e53935",
  VIDRO: "#43a047",
  METAL: "#fb8c00",
  ELETRONICO: "#8e24aa",
};

Chart.defaults.font.family = '"Poppins", "Segoe UI", sans-serif';
Chart.defaults.color = "#5b6b5b";

function diasDoPeriodo(qtd) {
  const dias = [];
  const hoje = new Date();
  for (let i = qtd - 1; i >= 0; i--) {
    const d = new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate() - i);
    dias.push(d.toLocaleDateString("sv-SE")); // AAAA-MM-DD
  }
  return dias;
}

async function carregarPainel() {
  const p = await api("/api/relatorios/painel");

  document.getElementById("dias").textContent = p.dias;
  document.getElementById("kpi-kg").textContent = fmtKg(p.resumo.total_kg);
  document.getElementById("kpi-entregas").textContent = fmtNum(p.resumo.entregas);
  document.getElementById("kpi-cidadaos").textContent = fmtNum(p.resumo.cidadaos);
  document.getElementById("kpi-pontos").textContent = fmtNum(p.resumo.total_pontos);

  // Preenche os dias sem entrega com zero para o eixo ficar contínuo
  const porDia = Object.fromEntries(p.porDia.map((d) => [String(d.dia).slice(0, 10), Number(d.total_kg)]));
  const dias = diasDoPeriodo(p.dias);
  new Chart(document.getElementById("grafico-dia"), {
    type: "bar",
    data: {
      labels: dias.map((d) => d.slice(8, 10) + "/" + d.slice(5, 7)),
      datasets: [{ label: "kg", data: dias.map((d) => porDia[d] || 0), backgroundColor: "#43a047", borderRadius: 4, maxBarThickness: 18 }],
    },
    options: {
      maintainAspectRatio: false,
      plugins: { legend: { display: false } },
      scales: { y: { beginAtZero: true, ticks: { callback: (v) => v + " kg" } }, x: { grid: { display: false } } },
    },
  });

  new Chart(document.getElementById("grafico-material"), {
    type: "doughnut",
    data: {
      labels: p.porMaterial.map((m) => NOMES_MATERIAL[m.material]),
      datasets: [{
        data: p.porMaterial.map((m) => Number(m.total_kg)),
        backgroundColor: p.porMaterial.map((m) => CORES_MATERIAL[m.material]),
        borderColor: "#fff",
        borderWidth: 2,
      }],
    },
    options: {
      maintainAspectRatio: false,
      cutout: "60%",
      plugins: {
        legend: { position: "right" },
        tooltip: { callbacks: { label: (c) => ` ${c.label}: ${fmtKg(c.parsed)}` } },
      },
    },
  });

  document.getElementById("ranking").innerHTML = p.ranking.length
    ? p.ranking
        .map(
          (r, i) => `<tr>
            <td>${i + 1}º</td>
            <td>${escapar(r.nome)}</td>
            <td class="num">${fmtKg(r.total_kg)}</td>
            <td class="num moedas">${fmtNum(r.pontos)}</td>
          </tr>`
        )
        .join("")
    : '<tr><td colspan="4" class="vazio">Sem entregas no período.</td></tr>';
}

exigirLogin(["ADMIN"]).then((u) => u && carregarPainel());
