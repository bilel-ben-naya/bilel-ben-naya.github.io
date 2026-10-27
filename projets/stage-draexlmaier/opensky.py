import requests
from config import *

def fetch_flights():
    """
    Interroge l'API OpenSky Network pour récupérer les données de trafic aérien en temps réel sur la zone définie dans config.py.

    Returns:
        dict: données brutes de l'API contenant les états des aéronefs
        None: en cas d'erreur réseau ou HTTP

    Raises :
        requests.exceptions.Timeout: si le serveur ne repond pas en 5s
        requests.exceptions.HTTPError: si le serveur retourne une erreur HTTP
    """
    try:
        response = requests.get(BASE_URL, params=PARAMS, timeout=TIMEOUT)
        response.raise_for_status()
        data = response.json()
        return data

    except requests.exceptions.Timeout:
        return "❌ Erreur réseau : la requête a expiré"
    except requests.exceptions.HTTPError as e:
        print(f"❌ Erreur : {e}")
    except requests.exceptions.RequestException as e:
        print(f"❌ Erreur : {e}")

def parse_flights(data):
    """
    Vas a partir du dictionnaire brut traiter les donnees pour avoir une liste de dictionnaire par avions en excluant les avions au sol (on_ground = True)

    Args :
        data (dict) : donnees brutes retournees par fetch_flights()

    Returns :
        list : liste de dictionnaire contenant pour chaque avions callsign, country, altitude_m, speed_kmh, heading
    """
    flights = []
    if data is None or data.get("states") is None:
        return flights
    for flight in data['states']:
        if not flight[8]:
            flight_dict = {
                "callsign": flight[1].strip() or "None",
                "country": flight[2],
                "lat": flight[6] or 0,
                "lon": flight[5] or 0,
                "altitude_m": round(flight[7] or 0),
                "speed_kmh": int((flight[9] or 0) * 3.6),
                "heading": round(flight[10] or 0),
            }
            flights.append(flight_dict)
    return flights

def display_flights(flights):
    """
    A partir de la liste des avions vas les afficher de facon graphique avec chaque donnees bien allignee

    Args :
        flights (list) : liste de dictionnaire avec les information de chaque vols

    Returns :
        None
    """
    print(f"==================================================\n   AERIAL TRAFFIC - TUNISIA (real time)\n   flight detected : {len(flights)}\n==================================================\n")
    for flight in flights:
        print(f"✈ {flight['callsign']:<8}| {flight['country']:<25} | Alt : {flight['altitude_m']: >8} m | Speed : {flight['speed_kmh']: >5} km/h | Heading : {flight['heading']: > 5}°|")
    print("\n==================================================\n")

def main():
    data = fetch_flights()
    display_flights(parse_flights(data))

main()