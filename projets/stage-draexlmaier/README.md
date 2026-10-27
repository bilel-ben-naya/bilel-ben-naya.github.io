# Projets du stage DRÄXLMAIER (Sousse, 2026)

Outils écrits seul pendant le stage, en Python :

- `opensky.py`, `app.py` : scanner d'avions au-dessus de la Tunisie avec l'API OpenSky (ligne de commande et tableau de bord Streamlit avec carte Folium).
- `classifier.py`, `app_tickets.py`, `tickets.csv` : classifieur de tickets informatiques (TF-IDF et Naive Bayes). Les tickets sont simulés.
- `rag.py`, `document_chat.py` : questions sur un document (LangChain, FAISS, embeddings multilingues, Llama 3.1 via Groq).
- `unite.py`, `llm.py` : assistant en ligne de commande (résumé, question sur un document, classement de ticket).

Les clés d'API se mettent dans un fichier `.env` (non publié) : `GROQ_API_KEY`.

Démos dans le navigateur : https://bilel-ben-naya.github.io/#stage
