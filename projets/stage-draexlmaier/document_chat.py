import os
import tkinter as tk
from tkinter import filedialog
from docx import Document
from pypdf import PdfReader


def extraire_texte(chemin):
    """
    Cette fontion permet de recuper le texte d'un fichier en txt, pdf ou docx et le renvoyer sous forme de chaine de caractere

    Args :
        chemin(str) : le chemin pour acceder au fichier dans la machine

    Returns :
        texte(str) : le contenu complet du document
    """
    extension = os.path.splitext(chemin)[1].lower()
    texte = ""
    if extension == ".txt":
        with open(chemin, "r", encoding="utf-8") as f:
            return f.read()
    elif extension == ".pdf":
        reader = PdfReader(chemin)
        for page in reader.pages:
            texte += page.extract_text()
        return texte
    elif extension == ".docx":
        doc = Document(chemin)
        for para in doc.paragraphs:
            texte += para.text
        return texte
    else :
        return "format non pris en charge"

def get_texte():
    """
    cette fonction vas demander via le CLI un texte a founir et vas tout d'abord demander de quel facon il veut le founir(l'ecrire, founir un fichier) et vas le renvoyer dans un format str

    Args :
        None

    Returns :
        texte(str) : le texte forni par l'utilisateur
    """
    mode = input("Comment voulez-vous fournir le texte ?\nA. Ecrire le texte manuellement\nB. Ecrire le chemin du fichier\nC. Importer un fichier (PDF, TXT, DOCX)\nChoix du mode : ")
    if mode == "A":
        fin = False
        print("Entrez le texte (lorsque vous avez fini ecrivez '---' seul sur une ligne) : ")
        lignes = []
        while not fin:
            ligne = input()
            if ligne == "---":
                fin = True
            else:
                lignes.append(ligne)
        texte = "\n".join(lignes)
    elif mode == "B":
        chemin = input("Quel est le chemin pour acceder au fichier")
        texte = extraire_texte(chemin)
    elif mode == "C":
        root = tk.Tk()
        root.attributes('-topmost', True)
        root.withdraw()
        chemin = filedialog.askopenfilename(
            title = "Selectionner un fichier",
            filetypes=[
                ("Tous les formats", "*.pdf *.txt *.docx"),
                ("PDF", "*.pdf"),
                ("Text", "*.txt"),
                ("Word", "*.docx"),
            ]
        )
        root.destroy()
        if chemin:
            texte = extraire_texte(chemin)
        else:
            print("Aucun fichier selectionne\n")
            return get_texte()
    else :
        print("la selection n'est pas bonne recommancer\n")
        return get_texte()
    return texte
