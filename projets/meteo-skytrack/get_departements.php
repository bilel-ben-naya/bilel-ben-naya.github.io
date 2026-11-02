<?php
// Charger les départements depuis le fichier CSV
function getDepartements($region) {
    $departements = [];
    if (($handle = fopen('data/departement.csv', 'r')) !== false) {
        while (($data = fgetcsv($handle, 1000, ',')) !== false) {
            $regionId = $data[2];  // ID de la région
            if ($regionId == $region) {
                $departements[] = $data[1]; // Nom du département
            }
        }
        fclose($handle);
    }
    return $departements;
}

// Récupérer la région passée en paramètre
$region = $_GET['region'] ?? null;

$departements = [];
if ($region) {
    $departements = getDepartements($region);
    echo json_encode(['departements' => $departements]);
} else {
    echo json_encode(['error' => 'Région non trouvée']);
}
?>
