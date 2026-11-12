// Tela de login: envia e-mail/senha ao Java, que devolve qual painel abrir.
const form = document.getElementById("form-login");
const mensagem = document.getElementById("mensagem");
const botao = document.getElementById("btn-entrar");

form.addEventListener("submit", async (evento) => {
  evento.preventDefault();
  mensagem.classList.add("oculto");
  botao.disabled = true;
  try {
    const usuario = await api("/api/login", {
      method: "POST",
      body: {
        email: document.getElementById("email").value,
        senha: document.getElementById("senha").value,
      },
    });
    // Polimorfismo na prática: cada perfil devolve sua própria página inicial
    window.location.href = usuario.paginaInicial;
  } catch (e) {
    mensagem.textContent = e.message;
    mensagem.classList.remove("oculto");
    botao.disabled = false;
  }
});

document.querySelectorAll(".contas-demo button").forEach((b) =>
  b.addEventListener("click", () => {
    document.getElementById("email").value = b.dataset.email;
    document.getElementById("senha").value = "123456";
  })
);

// Se já estiver logado, pula direto para o painel
api("/api/me").then((u) => (window.location.href = u.paginaInicial)).catch(() => {});
