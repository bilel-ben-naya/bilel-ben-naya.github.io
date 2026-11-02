<?php

/**
 * Récupère une ou plusieurs images aléatoires dans un dossier donné.
 */
function getRandomImage($dir = "../images/photos/", $count = 1) {
    // Code ici
}

/**
 * Récupère la liste des régions depuis region.csv
 * @return array Liste des noms de régions
 */
function getRegions() {
    $file = __DIR__ . '/../data/region.csv';
    $regions = [];

    if (!file_exists($file)) return $regions;

    if (($handle = fopen($file, 'r')) !== false) {
        fgetcsv($handle); // skip header
        while (($row = fgetcsv($handle)) !== false) {
            if (isset($row[2])) {
                $regions[] = trim($row[2]); // name
            }
        }
        fclose($handle);
    }

    sort($regions);
    return array_unique($regions);
}


/**
 * Récupère les départements d'une région donnée depuis departement.csv
 */
function getDepartements($regionNom) {
    $departements = [];
    $regionCode = null;

    $regionFile = __DIR__ . '/../data/region.csv';
    if (($handle = fopen($regionFile, 'r')) !== false) {
        fgetcsv($handle);
        while (($row = fgetcsv($handle)) !== false) {
            if (isset($row[2]) && trim($row[2]) === trim($regionNom)) {
                $regionCode = trim($row[1]); // code
                break;
            }
        }
        fclose($handle);
    }

    if (!$regionCode) return [];

    $deptFile = __DIR__ . '/../data/department.csv';
    if (!file_exists($deptFile)) return [];

    if (($handle = fopen($deptFile, 'r')) !== false) {
        fgetcsv($handle); // skip header
        while (($row = fgetcsv($handle)) !== false) {
            if (isset($row[1], $row[3]) && trim($row[1]) === $regionCode) {
                $departements[] = trim($row[3]); // name
            }
        }
        fclose($handle);
    }

    sort($departements);
    return array_unique($departements);
}


/**
 * Récupère les villes d'un département donné depuis cities.csv
 */
function getVilles($departementNom) {
    $villes = [];
    $deptCode = null;

    $deptFile = __DIR__ . '/../data/department.csv';
    $citiesFile = __DIR__ . '/../data/cities.csv';

    if (!file_exists($deptFile) || !file_exists($citiesFile)) return $villes;

    // Trouver le code du département à partir de son nom
    if (($handle = fopen($deptFile, 'r')) !== false) {
        fgetcsv($handle);
        while (($row = fgetcsv($handle)) !== false) {
            if (isset($row[3]) && trim($row[3]) === trim($departementNom)) {
                $deptCode = trim($row[2]); // code
                break;
            }
        }
        fclose($handle);
    }

    if (!$deptCode) return [];

    // Récupérer les villes correspondant au code département
    if (($handle = fopen($citiesFile, 'r')) !== false) {
        fgetcsv($handle); // skip header
        while (($row = fgetcsv($handle)) !== false) {
            if (isset($row[1], $row[4]) && trim($row[1]) === $deptCode) {
                $villes[] = trim($row[4]); // name
            }
        }
        fclose($handle);
    }

    $villes = array_unique($villes);
    sort($villes);
    return $villes;
}

function getMeteo($ville) {
    $apiKey = 'VOTRE_CLE_WEATHERAPI';

    $ville = str_replace(['’', '\''], '-', $ville);
    $ville = str_replace([' sur ', ' Sur '], '-sur-', $ville);
    $villeEncodee = urlencode($ville . ",France");

    $apiUrl = "https://api.weatherapi.com/v1/forecast.json?key=$apiKey&q=$villeEncodee&days=7&lang=fr";
    $response = @file_get_contents($apiUrl);
    $data = $response ? json_decode($response, true) : null;

    if (!$data || !isset($data['current'], $data['forecast']['forecastday'])) {
        error_log("API WeatherAPI KO pour $ville");
        return null;
    }

    return [
        'current' => $data['current'],
        'forecast' => $data['forecast'] 
    ];
}


function getWeatherIcon($description, $iconCode = 'unknown') {
    $desc = strtolower(trim($description));

    $icons = [
        'soleil' => 'images/icons/sun.svg',
        'ensoleillé' => 'images/icons/sun.svg',
        'clair' => 'images/icons/sun.svg',
        'nuage' => 'images/icons/cloud-sun.svg',
        'pluie' => 'images/icons/rain.svg',
        'averse' => 'images/icons/rain.svg',
        'orage' => 'images/icons/storm.svg',
        'vent' => 'images/icons/wind.svg',
        'neige' => 'images/icons/snow.svg',
        'brouillard' => 'images/icons/fog.svg',
        'alerte' => 'images/icons/alert.svg',
    ];

    foreach ($icons as $pattern => $path) {
        if (strpos($desc, $pattern) !== false && file_exists(__DIR__ . '/../' . $path)) {
            return $path;
        }
    }

    // fallback vers l'icône WeatherAPI si aucune locale ne correspond
    $iconCode = $iconCode !== 'unknown' ? $iconCode : '113';
    return "https://cdn.weatherapi.com/weather/64x64/day/$iconCode.png";
}
/**
 * Récupère la ville et région à partir de l’adresse IP de l’utilisateur
 * Utilise ipinfo.io (gratuite avec token)
 *
 * @param string|null $ip Adresse IP (si null, récupérée automatiquement)
 * @return array|null Données géo ou null si erreur
 */
function getVilleParIp($ip = null) {
    if (!$ip) $ip = $_SERVER['REMOTE_ADDR'] ?? '127.0.0.1';

    
    $token = "VOTRE_TOKEN_IPINFO";
    $url = "https://ipinfo.io/{$ip}/json?token={$token}";

    $response = @file_get_contents($url);
    if (!$response) return null;

    $data = json_decode($response, true);
    if (!$data || !isset($data['city'])) return null;

    return [
        'ville'     => $data['city'] ?? null,
        'region'    => $data['region'] ?? null,
        'pays'      => $data['country'] ?? null,
        'latitude'  => isset($data['loc']) ? explode(',', $data['loc'])[0] : null,
        'longitude' => isset($data['loc']) ? explode(',', $data['loc'])[1] : null
    ];
}


// Fonction pour obtenir et incrémenter le nombre de hits
function getHitCount() {
    // Vérifier si le cookie 'hits' existe
    if (isset($_COOKIE['hits'])) {
        // Si le cookie existe, l'incrémenter
        $hitCount = (int)$_COOKIE['hits'] + 1;
    } else {
        // Si le cookie n'existe pas, initialiser à 1
        $hitCount = 1;
    }
    
    // Mettre à jour le cookie 'hits' avec la nouvelle valeur
    setcookie('hits', $hitCount, time() + 3600 * 24 * 30, "/"); // Cookie valide pendant 30 jours
    
    return $hitCount;
}


function logCitySearch($city) {
    $csvFile = __DIR__ . '/../data/cities.csv';
    $timestamp = date('Y-m-d H:i:s');
    $data = [$timestamp, $city];

    if (!file_exists(dirname($csvFile))) {
        mkdir(dirname($csvFile), 0755, true);
    }

    $file = fopen($csvFile, 'a');
    if ($file) {
        fputcsv($file, $data);
        fclose($file);
    }
}

function getCityStats() {
    $csvFile = __DIR__ . '/../data/cities.csv';
    $stats = [];

    if (file_exists($csvFile)) {
        $file = fopen($csvFile, 'r');
        while (($row = fgetcsv($file)) !== false) {
            if (isset($row[1])) {
                $city = trim($row[4]);
                $stats[$city] = ($stats[$city] ?? 0) + 1;
            }
        }
        fclose($file);
    }

    arsort($stats); // tri décroissant
    return $stats;
}

?>