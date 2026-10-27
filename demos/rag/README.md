# Déployer le relais (gratuit)

1. Créer une clé Groq dédiée à la démo (console.groq.com > API Keys) et fixer une limite de dépense dans les paramètres du compte.
2. Sur dash.cloudflare.com : Workers & Pages > Create > Worker. Coller le contenu de `relay-worker.js`, puis Deploy.
3. Settings > Variables and Secrets : ajouter le secret `GROQ_API_KEY` (la clé de l'étape 1) et la variable `ALLOWED_ORIGINS` = `https://bilel-ben-naya.github.io`.
4. Copier l'adresse du Worker (`https://....workers.dev`) dans `config.js` : `window.RAG_RELAY = "https://....workers.dev";`
