<?php 
    $pageTitle = 'SkyTrack - Prévisions Météo';
    include "./include/header.inc.php"; 
?>

<main>
    <section class="hero">
        <div class="hero-content">
            <h2>Suivez la Météo en Temps Réel</h2>
            <p>Consultez les prévisions météorologiques pour votre ville et restez informé des conditions climatiques de la semaine.</p>
            <a href="tech.php" class="btn">Tester les APIs Météo</a>
        </div>
    </section>

    <section class="about">
        <h2>À propos du site</h2>
        <p>Notre site web vous permet d'accéder facilement aux prévisions météo locales et globales. Nous utilisons des APIs publiques pour afficher les données météo en temps réel et d'autres informations utiles, comme votre géolocalisation approximative.</p>
        <p>Grâce à une interface simple et conviviale, vous pouvez consulter les prévisions pour aujourd'hui et les jours à venir pour toutes les villes de France.</p>
    </section>

    <section class="features">
        <h2>Fonctionnalités principales</h2>
        <div class="feature-item">
            <h3>Prévisions Météo</h3>
            <p>Obtenez les prévisions du jour et des jours suivants en fonction de votre ville. Les données sont actualisées en temps réel grâce aux APIs météo.</p>
            <a href="map.php" class="btn">Les Prévisions de SkyTrack</a>
        </div>
        <div class="feature-item">
            <h3>Géolocalisation</h3>
            <p>Visualisez votre position approximative et découvrez la météo pour votre région grâce à notre fonctionnalité de géolocalisation basée sur votre adresse IP.</p>
        </div>
        <div class="feature-item">
            <h3>Image du jour (APOD)</h3>
            <p>Découvrez chaque jour une image ou une vidéo fascinante de l'Univers fournie par la NASA (APOD).</p>
        </div>
    </section>

    <section class="recent-weather">
        <h2>Dernières Météos Consultées</h2>
        <div id="recent-weather-list">
            <?php
            require_once "include/functions.inc.php";
            $recent = array_slice(getCityStats(), 0, 6); // les 6 dernières villes

            foreach ($recent as $city => $count) {
                $meteo = getMeteo($city);
                if ($meteo): ?>
                    <div class="city-weather">
                        <h3><?= htmlspecialchars($city) ?></h3>
                        <img src="<?= getWeatherIcon($meteo['current']['condition']['text']) ?>" alt="<?= htmlspecialchars($meteo['current']['condition']['text']) ?>">
                        <p><strong>Température :</strong> <?= $meteo['current']['temp_c'] ?>°C</p>
                        <p><strong>Conditions :</strong> <?= htmlspecialchars($meteo['current']['condition']['text']) ?></p>
                    </div>
                <?php endif;
            }
            ?>
        </div>
    </section>


    <section class="cta">
        <h2>Envie de découvrir plus ?</h2>
        <p>Accédez à la page technique pour tester l'intégration des différentes APIs et explorez notre collection d'images et de vidéos spatiales.</p>
        <a href="tech.php" class="btn">Accéder à la page technique</a>
    </section>
</main>

<?php 
    require "./include/footer.inc.php"; 
?>


<script>
// Liste des grandes villes de France
const cities = ['Paris', 'Marseille', 'Lyon', 'Toulouse', 'Nice', 'Nantes'];

// Votre clé API OpenWeatherMap
const apiKey = 'VOTRE_CLE_OPENWEATHERMAP'; 

// Fonction pour récupérer la météo de chaque ville
function getWeather(city) {
    const apiUrl = `https://api.openweathermap.org/data/2.5/weather?q=${city}&appid=${apiKey}&units=metric&lang=fr`;

    fetch(apiUrl)
        .then(response => response.json())
        .then(data => {
            const weatherData = {
                city: data.name,
                temperature: data.main.temp,
                description: data.weather[0].description,
                icon: data.weather[0].icon
            };

            // Afficher les données météo dans la section
            displayCityWeather(weatherData);
        })
        .catch(error => {
            console.error("Erreur lors de la récupération des données météo", error);
        });
}

// Fonction pour afficher la météo de chaque ville
function displayCityWeather(weatherData) {
    const weatherContainer = document.getElementById('weather-cities-list');

    const cityWeather = document.createElement('div');
    cityWeather.classList.add('city-weather');
    
    cityWeather.innerHTML = `
        <h3>${weatherData.city}</h3>
        <img src="http://openweathermap.org/img/wn/${weatherData.icon}.png" alt="${weatherData.description}">
        <p><strong>Température:</strong> ${weatherData.temperature}°C</p>
        <p><strong>Conditions:</strong> ${weatherData.description}</p>
    `;

    weatherContainer.appendChild(cityWeather);
}

// Appeler la fonction pour chaque ville
cities.forEach(city => getWeather(city));

</script>