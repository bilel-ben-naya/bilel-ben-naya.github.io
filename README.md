# Portfolio de Bilel BEN NAYA

Site : https://bilel-ben-naya.github.io

Étudiant en Master 1 Data / IA (École du Numérique, Université Catholique de Lille), à la recherche d'une alternance de 24 mois en Data / IA. Chaque projet du site a une démo qui s'exécute directement dans le navigateur, et son code source est dans ce dépôt.

Contact : bilel.ben.naya@hotmail.com, https://www.linkedin.com/in/bilel-ben-naya

## Projets

| Projet | Technologies | Démo | Code |
|---|---|---|---|
| SkyTrack, site météo | PHP, JavaScript, API météo | `demos/meteo` | `projets/meteo-skytrack` |
| Xiangqi, échecs chinois | Java, Swing | `demos/xiangqi` | `projets/xiangqi` |
| Parking connecté intelligent | Arduino, capteurs, Python | `demos/parking` | `projets/parking-iot` |
| Communication Morse entre STM32 | C, STM32F4, UART | `demos/morse` | `projets/morse-stm32` |
| PlaneSpotter, application Android | Java, osmdroid, OpenSky | `demos/planespotter` | `projets/planespotter` |
| Simulateur d'ordonnancement CPU | C | `demos/cpu` | `projets/ordonnancement-cpu` |
| TP de déploiement réseau | FreeBSD, DNS, Squid | `demos/reseau` | `demos/reseau` |

Projets du stage chez DRÄXLMAIER (Sousse, mai à juillet 2026), réalisés seul :

| Projet | Technologies | Démo | Code |
|---|---|---|---|
| Scanner d'avions OpenSky | Python, Streamlit | `demos/opensky` | `projets/stage-draexlmaier` |
| Classifieur de tickets | scikit-learn, TF-IDF, Naive Bayes | `demos/tickets` | `projets/stage-draexlmaier` |
| Pipeline RAG | LangChain, FAISS | `demos/rag` | `projets/stage-draexlmaier` |
| UNITE | Python, LLM | `demos/rag` | `projets/stage-draexlmaier` |

La plupart des projets de cours sont des projets d'équipe, auxquels j'ai participé de bout en bout.

## Fonctionnement des démos

Les démos sont des ré-implémentations en JavaScript des programmes d'origine, vérifiées par comparaison avec les originaux :

- Morse (émetteur et récepteur) : 120 exécutions comparées au code C, aucune différence.
- Découpage de texte du RAG : 32 cas comparés à `RecursiveCharacterTextSplitter` de LangChain, aucune différence.
- Ordonnancement CPU et Xiangqi : mêmes règles et mêmes résultats que les programmes C et Java.

Les démos du site tournent dans des iframes isolées et peuvent s'ouvrir en fenêtre flottante ou en plein écran.

## Clés API

Aucune clé API n'est dans ce dépôt. Les anciennes clés des projets ont été remplacées par des valeurs à renseigner (`VOTRE_CLE_...`).

La démo RAG appelle un modèle de langage via un relais Cloudflare Worker (`demos/rag/relay-worker.js`) : la clé reste dans les secrets du Worker, le relais n'accepte que les requêtes venant de ce site, avec des consignes fixes, des limites de taille et un plafond de requêtes. Le déploiement est décrit dans `demos/rag/README.md`.

La démo OpenSky utilise un instantané réel de trafic aérien (`demos/opensky/snapshot.json`), car l'API publique d'OpenSky n'est pas accessible depuis un navigateur sur ce domaine.

## Structure

- `index.html` : le site (HTML, CSS et JavaScript, sans dépendance de build)
- `demos/` : une démo exécutable par projet
- `projets/` : le code source des projets
