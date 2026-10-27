"""
Ce script nous fourni une interface en ligne de commande (CLI) dans le terminal nous permettant d'utiliser toute les fonction de llm.py

Source : Groq API

Fonctionalites :
    classer un ticket automatiquement
    resumer un texte
    repondre a une question en se basant sur un texte

Usage :
    python unite.py
"""

from llm import classification, resumer, repondre
from document_chat import extraire_texte, get_texte

working = True
while working:
    print("╔══════════════════════════════════════╗\n║   UNITE — Assistant IA DRÄXLMAIER    ║\n╚══════════════════════════════════════╝")
    print("1. Résumer un texte\n2. Poser une question sur un document\n3. Classifier un ticket\n4. Quitter")

    choix = input("Votre choix : ")
    if choix == "4":
        working = False
    elif choix == "3":
        titre = input("Quel est le titre de votre ticket ?")
        description = input("Decrivez votre probleme : ")
        classification(titre, description)
        input("\nAppuyer sur entree pour continuer")
    elif choix == "2":
        texte = get_texte()
        question = input("Quel est votre question ? : ")
        print(repondre(texte,question))
        input("\nAppuyer sur entree pour continuer")
    elif choix == "1":
        texte = get_texte()
        print(resumer(texte))
        input("\nAppuyer sur entree pour continuer")
    else:
        print("\nVeuiller choisir un mode valide \n")
