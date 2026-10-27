# draexlmaier-api-project

## Contenu du repo

### 1. exercices API & JSON (main.py)
### 2. OpenSky Flight Tracker
### 3. Classificateur de tickets ML

## Installation

pip install requests
pip install pandas
pip install streamlit
pip install folium
pip install streamlit-folium
pip install scikit-learn

## Projet 1 - Exercice API & JSON

### Description

Cet exercice est une initialisation a l'utilisation des API et consiste en la 
recuperation d'user de traiter ces donnees pour les afficher correctement et de
pouvoir avoir le nombre d'user dans une entreprise contenant "Group" dans leur nom

### Fichier

- main.py

### Lancement

python main.py

## Projet 2 - OpenSky Flight Tracker

### Description

un trackeur des avions au dessus de la Tunisie en direct en utilisant l'API public du reseau OpenSky

### Fichiers

- opensky.py
- app.py

### Lancement 

python opensky.py (pour l'interface machine)
python -m streamlit run app.py (pour l'interface graphique)

## Projet 3 - Classification de tickets

### Description

simulation d'un systeme de classification de tickets qui a partir d'ancien ticket vas trier automatiquement des nouveau ticket

### Fichiers

- classifier.py
- app_tickets.py
- tickets.csv

### Lancement

python classifier.py (pour l'interface machine)
python -m streamlit run app_tickets.py (pour l'interface graphique)

## Technologie utilisees

- python 3.x
- requests library
- pandas library
- streamlit library
- scikit-learn (TF-IDF, MultinomialNB)
- folium + streamlit-folium (carte interactive)
