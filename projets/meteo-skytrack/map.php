<?php
$pageTitle = 'SkyTrack - Map';
require_once "include/functions.inc.php";
include "include/header.inc.php";

// Récupération des régions pour le chargement initial
$regions = getRegions();
$selectedRegion = $_GET['region'] ?? null;
$selectedDept = $_GET['departement'] ?? null;
$selectedVille = $_GET['ville'] ?? null;

// Initialisation météo
$meteo = null;

if ($selectedVille) {
    $meteo = getMeteo($selectedVille);
}
?>
<main>
    <section>

        <h2>Cliquez sur une région de la carte</h2>

        <div class="map-container">
            <figure>
                <img src="images/carte-france.png" usemap="#image-map" alt="Carte des régions de France">
            </figure>
        </div>

        <!--  MAP HTML AVEC  -->
        <map name="image-map">
            <!-- Nouvelle-Aquitaine -->
            <area target="" alt="Nouvelle-Aquitaine" title="Nouvelle-Aquitaine" href="map.php?region=Nouvelle-Aquitaine" coords="211,689,312,390,448,468,450,522,327,617,272,657" shape="poly">

            <!-- Occitanie -->
            <area target="" alt="Occitanie" title="Occitanie" href="map.php?region=Occitanie"  coords="293,732,419,569,603,639,479,771" shape="poly">

            <!-- Corse -->
            <area target="" alt="Corse" title="Corse" href="map.php?region=Corse" coords="837,668,849,748,830,799,790,742" shape="poly">

            <!-- Pays de la Loire -->
            <area target="" alt="Pays de la Loire" title="Pays de la Loire" href="map.php?region=Pays de la Loire" coords="275,253,370,291,216,407,204,335"  shape="poly">

            <!-- Provence-Alpes-Côte d'Azur -->
            <area target="" alt="Provence-Alpes-Côte d'Azur" title="Provence-Alpes-Côte d'Azur" href="map.php?region=Provence-Alpes-Côte d'Azur" coords="722,552,602,679,714,705,783,640"  shape="poly">

            <!-- Auvergne-Rhône-Alpes -->
            <area target="" alt="Auvergne-Rhône-Alpes" title="Auvergne-Rhône-Alpes" href="map.php?region=Auvergne-Rhône-Alpes"  coords="481,522,589,604,749,514,707,444,474,426"  shape="poly">

            <!-- Bretagne -->
            <area target="" alt="Bretagne" title="Bretagne" href="map.php?region=Bretagne" coords="64,216,242,235,243,288,169,311,69,255"  shape="poly">

            <!-- Centre-Val de Loire -->
            <area target="" alt="Centre-Val de Loire" title="Centre-Val de Loire" href="map.php?region=Centre-Val de Loire"  coords="404,232,343,340,413,420,485,361,483,278" shape="poly">

            <!-- Normandie -->
            <area target="" alt="Normandie" title="Normandie" href="map.php?region=Normandie" coords="234,161,416,126,413,200,355,248,249,223" shape="poly">

            <!-- Bourgogne-Franche-Comté -->
            <area target="" alt="Bourgogne-Franche-Comté" title="Bourgogne-Franche-Comté" href="map.php?region=Bourgogne-Franche-Comté"  coords="518,289,724,319,685,420,506,397" shape="poly">

            <!-- Île-de-France -->
            <area target="" alt="Île-de-France" title="Île-de-France" href="map.php?region=Île-de-France"  coords="420,187,508,202,514,242,487,264,435,237,422,211"  shape="poly">

            <!-- Hauts-de-France -->
            <area target="" alt="Hauts-de-France" title="Hauts-de-France" href="map.php?region=Hauts-de-France" coords="424,37,557,85,525,191,436,169,421,89" shape="poly">

            <!-- Grand Est -->
            <area target="" alt="Grand Est" title="Grand Est" href="map.php?region=Grand Est" coords="592,122,548,262,761,298,774,223,776,192"  shape="poly">
        </map>

        <h2 id="form-title">Sélectionnez un département et une ville</h2>

        <form method="get" action="meteo.php" id="meteo-form">
            <label for="region">Région :</label>
            <select name="region" id="region">
                <option value="">-- Choisir une région --</option>
                <?php foreach ($regions as $region): ?>
                    <option value="<?= htmlspecialchars($region) ?>" <?= $region === $selectedRegion ? 'selected' : '' ?>>
                        <?= htmlspecialchars($region) ?>
                    </option>
                <?php endforeach; ?>
            </select>

            <label for="departement">Département :</label>
            <select name="departement" id="departement">
                <option value="">-- Choisir un département --</option>
                <?php if ($selectedRegion): ?>
                    <?php $departements = getDepartements($selectedRegion); ?>
                    <?php foreach ($departements as $dept): ?>
                        <option value="<?= htmlspecialchars($dept) ?>" <?= $dept === $selectedDept ? 'selected' : '' ?>>
                            <?= htmlspecialchars($dept) ?>
                        </option>
                    <?php endforeach; ?>
                <?php endif; ?>
            </select>

            <label for="ville">Ville :</label>
            <select name="ville" id="ville">
                <option value="">-- Choisir une ville --</option>
                <?php if ($selectedDept): ?>
                    <?php $villes = getVilles($selectedDept); ?>
                    <?php foreach ($villes as $ville): ?>
                        <option value="<?= htmlspecialchars($ville) ?>" <?= $ville === $selectedVille ? 'selected' : '' ?>>
                            <?= htmlspecialchars($ville) ?>
                        </option>
                    <?php endforeach; ?>
                <?php endif; ?>
            </select>
        </form>

        <?php if ($meteo): ?>
        <h2> Météo actuelle à <?= htmlspecialchars($selectedVille) ?></h2>

        <div class="weather-cards-container">
            <!-- Carte météo actuelle -->
            <div class="weather-card-small">
                <div class="weather-icon">
                    <img src="<?= getWeatherIcon($meteo['current']['condition']['text']) ?>" alt="Icone météo actuelle">
                </div>
                <div class="weather-info">
                    <h3>Actuellement</h3>
                    <p class="temperature"><?= $meteo['current']['temp_c'] ?> °C</p>
                    <p class="description"><?= htmlspecialchars($meteo['current']['condition']['text']) ?></p>
                </div>
            </div>
        </div>

        <h3>Prévisions météo à 7 jours</h3>
        <div class="weekly-forecast">
            <?php foreach ($meteo['forecast'] as $jour): ?>
                <div class="daily-forecast-card">
                    <div class="forecast-date"><?= htmlspecialchars($jour['date']) ?></div>
                    <div class="forecast-icon">
                        <img src="<?= getWeatherIcon($jour['day']['condition']['text']) ?>" alt="<?= htmlspecialchars($jour['day']['condition']['text']) ?>">
                    </div>
                    <div class="forecast-temps">
                        <span class="temp-max"><?= $jour['day']['maxtemp_c'] ?>°C</span>
                        <span class="temp-min"><?= $jour['day']['mintemp_c'] ?>°C</span>
                    </div>
                    <div class="forecast-desc"><?= htmlspecialchars($jour['day']['condition']['text']) ?></div>
                </div>
            <?php endforeach; ?>
        </div>
        <?php endif; ?>
    </section>
</main>
<script>
// Attendre que le DOM soit chargé
document.addEventListener('DOMContentLoaded', () => {
    const areas = document.querySelectorAll('map area');
    const regionSelect = document.getElementById('region');
    const departementSelect = document.getElementById('departement');
    const villeSelect = document.getElementById('ville');
    const form = document.getElementById('meteo-form');
    const formTitle = document.getElementById('form-title');

    // Gérer les clics sur la carte pour mettre à jour l'URL et le formulaire
    areas.forEach(area => {
        area.addEventListener('click', (e) => {
            e.preventDefault(); // Empêche le rechargement de la page

            const region = area.getAttribute('title');  // Récupère la région cliquée

            // Mettre à jour l'URL de la page (modifie l'URL sans recharger la page)
            const newUrl = new URL(window.location.href);
            newUrl.searchParams.set('region', region);  // Ajoute la région dans l'URL
            window.history.pushState({ path: newUrl.href }, '', newUrl.href);  // Mise à jour de l'URL

            // Mettre à jour le menu de sélection des régions
            regionSelect.value = region;

            // Déclencher l'événement de changement pour charger les départements
            regionSelect.dispatchEvent(new Event('change'));

            // Faire défiler vers le formulaire
            formTitle.scrollIntoView({ behavior: 'smooth' });
        });
    });

    // Gérer le changement de département
    regionSelect.addEventListener('change', () => {
        const region = regionSelect.value;
        if (region) {
            // Mettre à jour l'URL avec la région sélectionnée
            const newUrl = new URL(window.location.href);
            newUrl.searchParams.set('region', region); // Ajoute ou met à jour la région
            window.history.pushState({ path: newUrl.href }, '', newUrl.href);

            // Charger les départements dynamiquement via fetch
            fetch(`meteo_fetch.php?region=${encodeURIComponent(region)}`)
                .then(response => response.json())
                .then(data => {
                    if (data.departements) {
                        // Mettre à jour le menu des départements
                        departementSelect.innerHTML = '<option value="">-- Choisir un département --</option>';
                        data.departements.forEach(dept => {
                            const option = document.createElement('option');
                            option.value = dept;
                            option.textContent = dept;
                            departementSelect.appendChild(option);
                        });
                    }
                });
        }
    });

    // Gérer le changement de département
    departementSelect.addEventListener('change', () => {
        const departement = departementSelect.value;
        if (departement) {
            // Mettre à jour l'URL avec le département sélectionné
            const newUrl = new URL(window.location.href);
            newUrl.searchParams.set('departement', departement); // Ajoute ou met à jour le département
            window.history.pushState({ path: newUrl.href }, '', newUrl.href);

            // Charger les villes dynamiquement via fetch
            fetch(`meteo_fetch.php?departement=${encodeURIComponent(departement)}`)
                .then(response => response.json())
                .then(data => {
                    if (data.villes) {
                        // Mettre à jour le menu des villes
                        villeSelect.innerHTML = '<option value="">-- Choisir une ville --</option>';
                        data.villes.forEach(ville => {
                            const option = document.createElement('option');
                            option.value = ville;
                            option.textContent = ville;
                            villeSelect.appendChild(option);
                        });
                    }
                });
        }
    });

    // Gérer le changement de ville (soumettre le formulaire pour afficher la météo)
    villeSelect.addEventListener('change', () => {
        const ville = villeSelect.value;
        if (ville) {
            // Mettre à jour l'URL avec la ville sélectionnée
            const newUrl = new URL(window.location.href);
            newUrl.searchParams.set('ville', ville); // Ajoute ou met à jour la ville
            window.history.pushState({ path: newUrl.href }, '', newUrl.href);

            // Soumettre le formulaire automatiquement pour afficher la météo
            form.submit();
        }
    });
});


</script>

<?php include "include/footer.inc.php"; ?>