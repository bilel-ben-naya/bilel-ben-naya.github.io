# ✈️ PlaneSpotter – Android Application

PlaneSpotter is an Android application that allows users to visualize live aircraft traffic on an interactive map using data from the **OpenSky Network API**.  
The application provides real-time aircraft positions, altitude-based visualization, and detailed flight information in a clean and intuitive interface.

---

## 📱 Main Features

### 🗺️ Live Aircraft Map
- Real-time display of aircraft positions on an interactive map.
- Aircraft icons are **oriented according to their real heading**.
- Each aircraft is colored based on its **altitude**, using a clear and high-contrast color gradient.
- Aircraft trajectories are displayed using the **same color as the aircraft**, improving visual tracking.

### 🎨 Altitude Color Legend
- A simplified altitude legend inspired by OpenSky.
- Gradient bar displayed on the map screen.
- Colors represent altitude ranges clearly and consistently.
- Altitude values are displayed above the legend.

### ✈️ Aircraft Details
- Tap on an aircraft to access a **dedicated details screen**.
- Information provided:
  - ICAO24 identifier
  - Callsign (flight number)
  - Origin country
  - Latitude & longitude
  - Altitude (barometric / geometric)
  - Speed
  - Heading
  - Vertical rate
  - On-ground status
- Each field is clearly labeled and explained for better understanding.
- A back arrow allows returning to the map easily.

### 🔐 Secure Connection
- Authentication via **OpenSky Network**.
- Login screen dedicated only to connection (no extra settings).
- Uses official OpenSky credentials.
- Prevents API rate-limit errors (429) with proper handling.

### ⚙️ Settings
- Dedicated settings screen for API configuration.
- Clear explanations for each parameter:
  - Request interval
  - Data refresh behavior
  - API limits awareness
- No impact on the connection screen for better UX separation.

### 🧭 Navigation
- Bottom navigation available on:
  - Map
  - Connection
  - Settings
- Plane details screen uses a **back arrow instead of bottom navigation**, for better UX consistency.

---

## 🔑 How to Connect (OpenSky)

1. Create an account on: https://opensky-network.org  
2. Open the PlaneSpotter application.
3. Go to the **Connection** tab.
4. Enter your **OpenSky username and password**.
5. Tap **Connect**.
6. Once authenticated, return to the **Map** to see live aircraft.

> ⚠️ Note: Anonymous access is limited. Using a valid OpenSky account is strongly recommended.

---

## 🧪 How to Use the Application

1. **Launch the app**
2. **Connect** using OpenSky credentials
3. Open the **Map** tab
4. Observe live aircraft:
   - Colors indicate altitude
   - Orientation shows heading
5. Tap on any aircraft to open the **details screen**
6. Use the back arrow to return to the map
7. Adjust API behavior in **Settings** if needed

---

## 🛠️ Technical Overview

- Language: **Java**
- Platform: **Android**
- API: **OpenSky Network REST API**
- Map engine: **OSMDroid**
- Architecture:
  - Clean separation between UI, data, and network layers
  - Centralized API client
  - Parcelable models for navigation

---

## 👨‍💻 Authors

- **Alexis SADE**  
  Email: alexissade555@gmail.com

- **Bilel BEN NAYA**  
  Email: bilel.ben.naya@hotmail.com

---

## 📄 License & Usage

This project was developed for educational purposes.  
All flight data belongs to the OpenSky Network.

---

## ✅ Project Status

✔ Fully functional  
✔ Stable build  
✔ Clean UI / UX  
✔ Ready for evaluation
