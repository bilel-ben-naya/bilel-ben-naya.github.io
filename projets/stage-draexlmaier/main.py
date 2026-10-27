"""
ce script recupere via une api des utilisateurs avec leur information et vas afficher pour chacun leur Nom, email, ville
et societe et ensuite nous dire le nombre d'user avec Group dans le nom de leur entreprise

Source : JSONPlaceholder API (https://jsonplaceholder.typicode.com)

Fonctionalites :
    - recupere les different user et les affiche de formater
    - peut compter le nombre d'user contenant un certain terme ici ceux avec "Group" dans le nom de l'entreprise

Usage : python main.py

Output : output.json
"""

import requests
import json

try:
    response = requests.get("https://jsonplaceholder.typicode.com/users", timeout=5)
    response.raise_for_status()
    sCode = response.status_code
    data = response.json()
    d = type(data)
    nbUsers = len(data)
    users = []

    print(f"Status code : {sCode}\nType : {d} \nNumber of users : {nbUsers}")
    for i, user in enumerate(data,start=1):
        print(f'--- utilisateur {i} ---\nNom : {user["name"]}\nEmail : {user["email"]}\nVille : {user["address"]["city"]}\nSociete : {user["company"]["name"]}\n')
    for user in data:
        if "Group" in user["company"]["name"]:
            infoUser = {
                "name": user["name"],
                "email": user["email"],
                "company": user["company"]["name"],
            }

            users.append(infoUser)

    with open("output.json","w",encoding="utf-8") as f:
        json.dump(users,f,ensure_ascii=False,indent=4)
    print(f"nombre user avec 'Group' dans le nom de leur entreprise : {len(users)}")

except requests.exceptions.Timeout:
    print("❌ Erreur réseau : la requête a expiré")
except requests.exceptions.HTTPError as e:
    print(f"❌ Erreur : {e}")
except requests.exceptions.RequestException as e:
    print(f"❌ Erreur : {e}")
