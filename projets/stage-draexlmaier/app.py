"""
Interface graphique Streamlit pour le suivi du trafic aérien en temps réel au dessus de la Tunisie.

Source : OpenSky Network API (https://opensky-network.org)

Fonctionnalites :
    - Affiche les avions en vol au dessus de la Tunisie en temps reel
    - Tableau interactif avec callsign, pays, altitude, vitesse et cap
    - Carte interactive avec position des avions via folium
    - Bouton de rafraichissement des donnees

Usage : python -m streamlit run app.py
"""

import streamlit as st
import pandas as pd
from streamlit_folium import st_folium
import folium
from opensky import *

# Charger les données seulement au premier lancement ou au Refresh
if "flights" not in st.session_state:
    data = fetch_flights()
    st.session_state.flights = parse_flights(data) if data else []

st.title("AERIAL TRAFFIC - TUNISIA (real time)")

if st.button("🔄 Refresh"):
    st.session_state.flights = parse_flights(fetch_flights())

flights = st.session_state.flights
df = pd.DataFrame(flights)


st.subheader(f"✈️ flight detected : {len(flights)}")
st.dataframe(df.drop(columns=["lat", "lon"], errors="ignore"))

m = folium.Map(location=[34.0, 9.5], zoom_start=6, tiles="CartoDB dark_matter")
for _, row in df.iterrows():
    folium.Marker(
        location=[row["lat"], row["lon"]],
        popup=f"{row['callsign']} — {row['country']}",
        icon=folium.DivIcon(html="<div style='font-size:20px'>✈️</div>")
    ).add_to(m)

st_folium(m, width=700, height=450)