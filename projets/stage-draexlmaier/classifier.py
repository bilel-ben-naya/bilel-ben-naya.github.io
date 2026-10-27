"""
Ce script permet de recuper les tickets dans le fichier tickets.csv et d'entrainer le programme pour l'assignation automatique des ticket prochain

Dataset : tickets.csv

Modele : MultinomialNB

Precision : 99%
"""

import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.model_selection import train_test_split
from sklearn.naive_bayes import MultinomialNB
from sklearn.metrics import accuracy_score

pd.set_option('display.max_columns', None)

df = pd.read_csv("tickets.csv")

df["texte"] = df["titre"] + "" + df["description"]

vectorizer = TfidfVectorizer()
X = vectorizer.fit_transform(df["texte"])
Y = df["equipe"]

X_train, X_test, y_train, y_test = train_test_split(X, Y, test_size=0.2, random_state=42)

model = MultinomialNB()
model.fit(X_train, y_train)

y_pred = model.predict(X_test)
print(f"Précision : {accuracy_score(y_test, y_pred) * 100:.1f}%")
print(f"Tickets d'entraînement : {X_train.shape[0]}")
print(f"Tickets de test : {X_test.shape[0]}")

nouveau_ticket = "Mon écran ne s'allume pas depuis ce matin"

X_nouveau = vectorizer.transform([nouveau_ticket])

prediction = model.predict_proba(X_nouveau)[0]
print(f"Ticket : '{nouveau_ticket}'")
for classe, proba in zip(model.classes_, prediction):
    print(f"{classe} : {proba*100:.1f}%")
