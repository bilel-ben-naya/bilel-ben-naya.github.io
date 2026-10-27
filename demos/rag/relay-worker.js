/* Relais Cloudflare Worker pour la démo RAG / UNITE.
   La clé Groq reste ici (secret GROQ_API_KEY), jamais dans le site.
   Le relais n'accepte que trois usages, avec les mêmes consignes que llm.py : repondre, resumer, classifier.
   Variables : GROQ_API_KEY (secret), ALLOWED_ORIGINS (ex. https://bilel-ben-naya.github.io). */

const MODEL = "llama-3.1-8b-instant";
const MAX_CHARS = 6000, MAX_TOKENS = 500;
const WINDOW_MS = 10 * 60 * 1000, MAX_PER_WINDOW = 20;
const hits = new Map();

const SYS = {
  repondre: "tu est un annalyste de texte et ton objectif est de pouvoir repondre a n'importe quelle question a partir du texte fourni et si la reponse ne se trouve pas dans le texte tu dois juste me dire que cette information n'est pas dans le texte. Tu a interdiction de donnee une information qui ne viens pas du texte",
  resumer: "tu est un annalyste de texte et ton objectif est de resumer un texte en 3 point maximum et le tout en francais de facon professionnel",
  classifier: 'Tu est un assistant IT chez un equipementier automobile et on but est de trier les ticket parmi une de ses 3 categorie : Reseau; Hardware; Logiciel. chaque tiquet a un titre et un description. Pour chacun des ticket tu vas aussi estimer sa priorite allant de P1 a P5 ou P1 est le plus urgent et aussi resumer le probleme en une phrase. Réponds UNIQUEMENT dans ce format JSON avec les 3 champs demander, sans texte supplémentaire : {"categorie": "Hardware|Réseau|Logiciel","priorite": "P1|P2|P3|P4|P5","resume": "une phrase"}',
};

function cors(origin) {
  return { "Access-Control-Allow-Origin": origin, "Access-Control-Allow-Methods": "POST, OPTIONS", "Access-Control-Allow-Headers": "Content-Type", "Vary": "Origin" };
}
function json(obj, status, origin) { return new Response(JSON.stringify(obj), { status, headers: { "Content-Type": "application/json", ...cors(origin) } }); }

export default {
  async fetch(req, env) {
    const origin = req.headers.get("Origin") || "";
    const allowed = (env.ALLOWED_ORIGINS || "https://bilel-ben-naya.github.io").split(",").map((s) => s.trim());
    if (!allowed.includes(origin)) return new Response("Origine refusée", { status: 403 });
    if (req.method === "OPTIONS") return new Response(null, { status: 204, headers: cors(origin) });
    if (req.method !== "POST") return json({ error: "Méthode non autorisée" }, 405, origin);

    const ip = req.headers.get("CF-Connecting-IP") || "?", now = Date.now();
    const list = (hits.get(ip) || []).filter((t) => now - t < WINDOW_MS);
    if (list.length >= MAX_PER_WINDOW) return json({ error: "Trop de requêtes, réessayez dans quelques minutes." }, 429, origin);
    list.push(now); hits.set(ip, list);

    let b; try { b = await req.json(); } catch { return json({ error: "JSON invalide" }, 400, origin); }
    const clip = (s) => String(s || "").slice(0, MAX_CHARS);
    let user;
    if (b.mode === "repondre") user = `\n    Texte : \n    ${clip(b.texte)} \n    Question : \n    ${clip(b.question).slice(0, 500)}\n    `;
    else if (b.mode === "resumer") user = clip(b.texte);
    else if (b.mode === "classifier") user = `\n    Titre : \n    ${clip(b.titre).slice(0, 300)} \n    Description : \n    ${clip(b.description).slice(0, 2000)}\n    `;
    else return json({ error: "Mode inconnu" }, 400, origin);

    const r = await fetch("https://api.groq.com/openai/v1/chat/completions", {
      method: "POST",
      headers: { "Content-Type": "application/json", Authorization: "Bearer " + env.GROQ_API_KEY },
      body: JSON.stringify({ model: MODEL, max_tokens: MAX_TOKENS, temperature: 0, messages: [{ role: "system", content: SYS[b.mode] }, { role: "user", content: user }] }),
    });
    if (!r.ok) return json({ error: "Le service de langage ne répond pas (" + r.status + ")." }, 502, origin);
    const d = await r.json();
    const text = (d.choices && d.choices[0] && d.choices[0].message.content) || "";
    if (b.mode === "classifier") {
      try { return json({ result: JSON.parse(text) }, 200, origin); } catch { return json({ text }, 200, origin); }
    }
    return json({ text }, 200, origin);
  },
};
