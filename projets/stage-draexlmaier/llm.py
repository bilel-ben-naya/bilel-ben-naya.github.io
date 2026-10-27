from groq import Groq
from dotenv import load_dotenv
import os
import json

load_dotenv()
client = Groq(api_key=os.getenv("GROQ_API_KEY"))

def classification(titre,description):
    """
    Cette fonction permet de grace a l'API de groq de classifier un ticket qu'on lui donne en suivant les consigne qu'on lui a donnee et nous affiche de facon concise les resultats

    Args :
        - titre(str) : le titre du ticket a classer
        - description(str) : la description du ticket

    Returns:
        None
    """
    response = client.chat.completions.create(
        model="llama-3.1-8b-instant",
        messages=[
            {"role": "system", "content": 'Tu est un assistant IT chez un equipementier automobile et on but est de trier les ticket parmi une de ses 3 categorie : Reseau; Hardware; Logiciel. chaque tiquet a un titre et un description. Pour chacun des ticket tu vas aussi estimer sa priorite allant de P1 a P5 ou P1 est le plus urgent et aussi resumer le probleme en une phrase. Réponds UNIQUEMENT dans ce format JSON avec les 3 champs demander, sans texte supplémentaire : {"categorie": "Hardware|Réseau|Logiciel","priorite": "P1|P2|P3|P4|P5","resume": "une phrase"}'},
            {"role": "user", "content":  f"""
    Titre : 
    {titre} 
    
    Description : 
    {description}
    """}
        ]
    )
    result = json.loads(response.choices[0].message.content)
    print(f"Catégorie : {result['categorie']}")
    print(f"Priorité  : {result['priorite']}")
    print(f"Résumé    : {result['resume']}")

def resumer(texte):
    """
    Cette fonction vas grace a l'API de groq resumer un texte en 3 point de facon professionnel

    Args :
        texte(str) : le texte que l'API vas devoir resumer

    Returns :
        le resumer structurer
    """
    response = client.chat.completions.create(
        model="llama-3.1-8b-instant",
        messages=[
            {"role": "system", "content": "tu est un annalyste de texte et ton objectif est de resumer un texte en 3 point maximum et le tout en francais de facon professionnel"},
            {"role": "user", "content": texte}
        ]
    )
    return response.choices[0].message.content

def repondre(texte, question):
    """
    Cette fonction vas a partir d;un texte qu'on lui donne repondre a une question en se basant seulement sur les information contenue dans le texte

    Args :
        texte(str) : le texte source sur lequel baser la reponse
        question(str) : la question

    Returns :
        la reponse de la question
    """
    response = client.chat.completions.create(
        model="llama-3.1-8b-instant",
        messages=[
            {"role": "system", "content": "tu est un annalyste de texte et ton objectif est de pouvoir repondre a n'importe quelle question a partir du texte fourni et si la reponse ne se trouve pas dans le texte tu dois juste me dire que cette information n'est pas dans le texte. Tu a interdiction de donnee une information qui ne viens pas du texte"},
            {"role": "user", "content": f"""
    Texte : 
    {texte} 
    
    Question : 
    {question}
    """
             }
        ]
    )
    return response.choices[0].message.content

