<?php 
    $pageTitle = 'SkyTrack - Tech';
    require "./include/header.inc.php"; 
?>

<main>
    <section>
        <h2>Image du jour - NASA (APOD)</h2>
        <?php
            // Utiliser la date actuelle pour la requête
            $date = date('Y-m-d');  // Format de date : YYYY-MM-DD
            $api_key = "VOTRE_CLE_NASA"; // Votre clé API personnelle
            $apod_url = "https://api.nasa.gov/planetary/apod?api_key=$api_key&date=$date"; // Utilisation de la date actuelle

            // Récupérer les données JSON de l'API
            $response = @file_get_contents($apod_url);

            // Vérifier si la requête a réussi
            if ($response === FALSE) {
                echo "Erreur lors de la récupération des données de l'API.";
            } else {
                $data = json_decode($response, true);

                // Vérifier si les données sont valides
                if (isset($data['url'], $data['title'], $data['explanation'])) {
                    $image_url = $data['url'];
                    $image_title = $data['title'];
                    $image_description = $data['explanation'];

                    // Afficher l'image ou la vidéo selon le type de média
                    if ($data['media_type'] == 'image') {
                        echo '<img src="' . $image_url . '" alt="Image de la NASA" style="max-width: 100%; height: auto;">';
                    } elseif ($data['media_type'] == 'video') {
                        // Vérifie si la vidéo est bien en format mp4
                        $video_url = $image_url; // Utilisation de l'URL de la vidéo retournée
                        if (strpos($video_url, '.mp4') !== false) {
                            echo '<video controls><source src="' . $video_url . '" type="video/mp4">Désolé, votre navigateur ne prend pas en charge la vidéo.</video>';
                        } else {
                            // Si le format n'est pas MP4, afficher un message d'erreur
                            echo "Le format de la vidéo n'est pas pris en charge.";
                        }
                    } else {
                        // Si le type de média est autre que image ou vidéo
                        echo "Le type de média n'est pas pris en charge.";
                    }

                    // Afficher la légende avec le titre et la description
                    echo '<figcaption><strong>' . $image_title . '</strong><br>' . $image_description . '</figcaption>';
                } else {
                    echo "Données manquantes dans la réponse de l'API.";
                }
            }
        ?>
    </section>

    <section>
        <h2>Localisation de l'utilisateur</h2>
        <div>
            <h3>Localisation estimée (GeoPlugin - XML)</h3>
            <?php
                // API GeoPlugin (XML)
                $geo_url = "http://www.geoplugin.net/xml.gp?ip={$_SERVER['REMOTE_ADDR']}"; // Récupérer l'IP de l'utilisateur
                $geo_response = @file_get_contents($geo_url);

                if ($geo_response === FALSE) {
                    echo "Impossible de récupérer les données de géolocalisation via GeoPlugin.";
                } else {
                    $xml = simplexml_load_string($geo_response);
                    $city = $xml->geoplugin_city;
                    $region = $xml->geoplugin_region;
                    $country = $xml->geoplugin_countryName;
                    $latitude = $xml->geoplugin_latitude;
                    $longitude = $xml->geoplugin_longitude;

                    echo "<p>Ville : " . htmlspecialchars($city) . "</p>";
                    echo "<p>Région : " . htmlspecialchars($region) . "</p>";
                    echo "<p>Pays : " . htmlspecialchars($country) . "</p>";
                    echo "<p>Latitude : " . htmlspecialchars($latitude) . "</p>";
                    echo "<p>Longitude : " . htmlspecialchars($longitude) . "</p>";
                }
            ?>
        </div>

        <div>
            <h3>Localisation via API JSON (ipinfo.io)</h3>
            <?php
                // API ipinfo.io (JSON)
                $ipinfo_url = "https://ipinfo.io/{$_SERVER['REMOTE_ADDR']}/json"; // Utilisation de l'IP de l'utilisateur
                $ipinfo_response = @file_get_contents($ipinfo_url);

                if ($ipinfo_response === FALSE) {
                    echo "Impossible de récupérer les données via ipinfo.io.";
                } else {
                    $ipinfo_data = json_decode($ipinfo_response, true);
                    $ipinfo_city = $ipinfo_data['city'] ?? 'Inconnue';
                    $ipinfo_region = $ipinfo_data['region'] ?? 'Inconnue';
                    $ipinfo_country = $ipinfo_data['country'] ?? 'Inconnu';
                    $ipinfo_coords = $ipinfo_data['loc'] ?? 'Inconnues';

                    echo "<p>Ville : " . htmlspecialchars($ipinfo_city) . "</p>";
                    echo "<p>Région : " . htmlspecialchars($ipinfo_region) . "</p>";
                    echo "<p>Pays : " . htmlspecialchars($ipinfo_country) . "</p>";
                    echo "<p>Coordonnées : " . htmlspecialchars($ipinfo_coords) . "</p>";
                }
            ?>
        </div>

        <div>
            <h3>Localisation via API XML (whatsmyip.com)</h3>
            <?php
                // API whatismyip.com (XML)
                $whatsmyip_url = "https://api.whatismyip.com/ip-address-lookup.php?key=VOTRE_CLE_WHATISMYIP&input={$_SERVER['REMOTE_ADDR']}&output=xml"; // Votre clé API
                $whatsmyip_response = @file_get_contents($whatsmyip_url);

                if ($whatsmyip_response === FALSE) {
                    echo "Impossible de récupérer les données via whatismyip.com.";
                } else {
                    $xml = simplexml_load_string($whatsmyip_response);
                    
                    // Vérification si les éléments XML existent
                    $whatsmyip_city = isset($xml->geoplugin_city) ? (string)$xml->geoplugin_city : 'Inconnue';
                    $whatsmyip_region = isset($xml->geoplugin_region) ? (string)$xml->geoplugin_region : 'Inconnue';
                    $whatsmyip_country = isset($xml->geoplugin_countryName) ? (string)$xml->geoplugin_countryName : 'Inconnu';

                    echo "<p>Ville : " . htmlspecialchars($whatsmyip_city) . "</p>";
                    echo "<p>Région : " . htmlspecialchars($whatsmyip_region) . "</p>";
                    echo "<p>Pays : " . htmlspecialchars($whatsmyip_country) . "</p>";
                }
            ?>
        </div>
    </section>
</main>

<?php 
    require "./include/footer.inc.php"; 
?>
