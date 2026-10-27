/**
 * Relais Cloudflare Worker pour l'API publique OpenSky (aucune clé nécessaire).
 * Pourquoi : OpenSky n'autorise le CORS que pour son propre site, donc un navigateur ne peut pas
 * l'appeler directement depuis github.io. Ce relais ajoute l'en-tête CORS pour ce site uniquement
 * et met la réponse en cache 15 s pour rester sous la limite de requêtes anonymes.
 *
 * Déploiement : Cloudflare > Workers > Créer > coller ce fichier > Déployer,
 * puis copier l'URL du worker dans demos/opensky/config.js (OPENSKY_RELAY).
 */
const ALLOWED = "https://bilel-ben-naya.github.io";
export default {
  async fetch(request) {
    const origin = request.headers.get("Origin");
    const cors = {
      "Access-Control-Allow-Origin": origin === ALLOWED ? ALLOWED : "null",
      "Access-Control-Allow-Methods": "GET, OPTIONS",
      "Vary": "Origin",
    };
    if (request.method === "OPTIONS") return new Response(null, { headers: cors });
    const u = new URL(request.url);
    const p = new URLSearchParams();
    for (const k of ["lamin", "lamax", "lomin", "lomax"]) {
      const v = Number(u.searchParams.get(k));
      if (!Number.isFinite(v)) return new Response("paramètre invalide : " + k, { status: 400, headers: cors });
      p.set(k, String(v));
    }
    const upstream = "https://opensky-network.org/api/states/all?" + p.toString();
    const res = await fetch(upstream, { cf: { cacheTtl: 15, cacheEverything: true } });
    return new Response(res.body, {
      status: res.status,
      headers: { ...cors, "Content-Type": "application/json", "Cache-Control": "public, max-age=15" },
    });
  },
};
