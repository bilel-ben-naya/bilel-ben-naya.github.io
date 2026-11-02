<?php
require_once "include/functions.inc.php";
include "include/header.inc.php";

// Récupération des paramètres GET
$selectedRegion = $_GET['region'] ?? null;
$selectedDept = $_GET['departement'] ?? null;
$selectedVille = $_GET['ville'] ?? null;

// Fallback : géolocalisation par IP si pas de ville
if (!$selectedVille) {
    $geo = getVilleParIp();
    if ($geo && $geo['ville']) {
        $selectedVille = $geo['ville'];
        $selectedRegion = $geo['region'] ?? null;
    }
}

// Récupération météo
$meteo = $selectedVille ? getMeteo($selectedVille) : null;
?>

<h2>Météo à <?= htmlspecialchars($selectedVille) ?> <?= $selectedDept ? '(' . htmlspecialchars($selectedDept) . ')' : '' ?></h2>

<?php if ($meteo): ?>
    <!-- Météo actuelle -->
    <div class="weather-cards-container">
        <div class="weather-card-large">
            <img src="<?= getWeatherIcon($meteo['current']['condition']['text']) ?>" alt="Météo">
            <h3>Actuellement</h3>
            <p class="temperature"><?= $meteo['current']['temp_c'] ?> °C</p>
            <p><?= htmlspecialchars($meteo['current']['condition']['text']) ?></p>
            <p>Vent : <?= $meteo['current']['wind_kph'] ?> km/h - Humidité : <?= $meteo['current']['humidity'] ?>%</p>
        </div>
    </div>

    <!-- Prévisions -->
    <h3>Prévisions sur 7 jours</h3>
    <?php if (isset($meteo['forecast']['forecastday']) && is_array($meteo['forecast']['forecastday'])): ?>
        <div class="weekly-forecast">
            <?php foreach ($meteo['forecast']['forecastday'] as $jour): ?>
                <div class="daily-forecast-card">
                    <div class="forecast-date"><?= htmlspecialchars($jour['date']) ?></div>
                    <div class="forecast-icon">
                        <img src="<?= getWeatherIcon($jour['day']['condition']['text']) ?>" alt="<?= htmlspecialchars($jour['day']['condition']['text']) ?>">
                    </div>
                    <div class="forecast-temps">
                        <span class="temp-max">Max : <?= round($jour['day']['maxtemp_c']) ?>°C</span><br>
                        <span class="temp-min">Min : <?= round($jour['day']['mintemp_c']) ?>°C</span><br>
                        <span class="humidity">Humidité : <?= $jour['day']['avghumidity'] ?>%</span><br>
                        <span class="wind">Vent : <?= $jour['day']['maxwind_kph'] ?> km/h</span><br>
                        <span class="feels">Temp. moyenne : <?= round($jour['day']['avgtemp_c']) ?>°C</span>
                    </div>
                    <div class="forecast-desc"><?= htmlspecialchars($jour['day']['condition']['text']) ?></div>
                </div>
            <?php endforeach; ?>
        </div>
    <?php else: ?>
        <p class="error">Aucune prévision météo disponible.</p>
    <?php endif; ?>
<?php else: ?>
    <p class="error">Aucune donnée météo trouvée pour cette ville.</p>
<?php endif; ?>

<?php include "include/footer.inc.php"; ?>
