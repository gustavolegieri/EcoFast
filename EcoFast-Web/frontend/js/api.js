// Funções comuns: falar com o back-end Java e controlar o login nas telas.

async function api(caminho, opcoes = {}) {
  const resposta = await fetch(caminho, {
    method: opcoes.method || "GET",
    headers: { "Content-Type": "application/json" },
    body: opcoes.body ? JSON.stringify(opcoes.body) : undefined,
  });
  const dados = await resposta.json().catch(() => ({}));
  if (!resposta.ok) {
    const erro = new Error(dados.erro || "Erro ao falar com o servidor.");
    erro.status = resposta.status;
    throw erro;
  }
  return dados;
}

// Garante que há alguém logado com um dos perfis permitidos; senão volta ao login.
async function exigirLogin(perfisPermitidos) {
  try {
    const usuario = await api("/api/me");
    if (!perfisPermitidos.includes(usuario.perfil)) {
      window.location.href = usuario.paginaInicial;
      return null;
    }
    const nome = document.getElementById("nome-usuario");
    if (nome) nome.textContent = usuario.nome;
    return usuario;
  } catch (e) {
    window.location.href = "/index.html";
    return null;
  }
}

async function sair() {
  await api("/api/logout", { method: "POST" }).catch(() => {});
  window.location.href = "/index.html";
}

const fmtKg = (v) => Number(v).toLocaleString("pt-BR", { minimumFractionDigits: 1, maximumFractionDigits: 1 }) + " kg";
const fmtNum = (v) => Number(v).toLocaleString("pt-BR");
const fmtData = (iso) => new Date(iso).toLocaleString("pt-BR", { day: "2-digit", month: "2-digit", hour: "2-digit", minute: "2-digit" });

const NOMES_MATERIAL = {
  PAPEL: "Papel / Papelão",
  PLASTICO: "Plástico",
  VIDRO: "Vidro",
  METAL: "Metal / Alumínio",
  ELETRONICO: "Lixo eletrônico",
};

const ICONES_MATERIAL = {
  PAPEL: "fa-newspaper",
  PLASTICO: "fa-bottle-water",
  VIDRO: "fa-wine-bottle",
  METAL: "fa-oil-can",
  ELETRONICO: "fa-microchip",
};

function escapar(texto) {
  const div = document.createElement("div");
  div.textContent = texto;
  return div.innerHTML;
}

document.addEventListener("DOMContentLoaded", () => {
  const botaoSair = document.getElementById("btn-sair");
  if (botaoSair) botaoSair.addEventListener("click", sair);
});
