"""
Ce programme vas decouper un texte donner en chucks pour pouvoir utiliser les embeddings sur chacum d'entre eux et ainsi
pouvoir d'apres une question selectionner les 2 chucks qui corresponde le plus et ensuite grace a la fonction repondre()
donner une reponse clair a partir de la partie pertinante du texte

Source : langchain

Fonctionnalite :
    repondre a une question en se bassant sur un texte donner

Utilisation :
    python rag.py
"""
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_community.vectorstores import FAISS
from document_chat import get_texte
from llm import repondre
from dotenv import load_dotenv
load_dotenv()

texte = get_texte()

splitter = RecursiveCharacterTextSplitter(chunk_size=500, chunk_overlap=50)

chunks = splitter.split_text(texte)

embeddings = HuggingFaceEmbeddings(model_name = "paraphrase-multilingual-MiniLM-L12-v2")

vectorstore = FAISS.from_texts(chunks,embeddings)

question = input("Quel est votre question ? : ")

resultats = vectorstore.similarity_search(question, k=2)

contexte = "\n\n".join([doc.page_content for doc in resultats])

print(repondre(contexte, question))
