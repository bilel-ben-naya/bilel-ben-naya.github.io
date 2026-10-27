"""
ce script permet d'avoir une interface graphique pour tester l'affectation automatique de tickets de support aux équipes IT. Il entraîne le modèle directement depuis tickets.csv.

Usage : python -m streamlit run app_tickets.py

Interface : streamlit

Modele : MultinomialNB
"""

import streamlit as st
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.naive_bayes import MultinomialNB

# Charger et entraîner
df = pd.read_csv("tickets.csv")
df["texte"] = df["titre"] + " " + df["description"]
vectorizer = TfidfVectorizer()
X = vectorizer.fit_transform(df["texte"])
model = MultinomialNB()
model.fit(X, df["equipe"])

# Interface
st.title("🎫 Système d'affectation de tickets")
st.subheader("Entrez un nouveau ticket")

titre = st.text_input("Titre du ticket")
description = st.text_area("Description du problème")

if st.button("Analyser"):
    if titre and description:
        # Prédiction
        texte = titre + " " + description
        X_nouveau = vectorizer.transform([texte])
        prediction = model.predict_proba(X_nouveau)[0]

        # Trouver l'équipe avec la probabilité max
        equipes = list(zip(model.classes_, prediction))
        meilleur = max(equipes, key=lambda x: x[1])
        equipe_assignee = meilleur[0]
        confiance = meilleur[1] * 100

        # Affichage résultat principal
        st.success(f"✅ Équipe assignée : **{equipe_assignee}** ({confiance:.1f}% de confiance)")

        # Affichage détail des scores
        st.divider()
        st.write("**Détail des probabilités :**")
        for equipe, proba in sorted(equipes, key=lambda x: x[1], reverse=True):
            st.progress(proba, text=f"{equipe} : {proba*100:.1f}%")
    else:
        st.warning("⚠️ Remplis le titre et la description !")