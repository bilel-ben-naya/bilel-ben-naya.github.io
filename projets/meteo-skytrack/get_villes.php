<?php
// Charger les villes depuis le fichier CSV
function getVilles($departement) {
    $villes = [];
    if (($handle = fopen('data/cities.csv', 'r')) !== false) {
        while (($data = fgetcsv($handle, 1000, ',')) !== false) {
            $departementId = $data[2];  // ID du département
            if ($departementId == $departement) {
                $villes[] = $data[4]; // Nom de la ville
            }
        }
        fclose($handle);
    }
    return $villes;
}

// Récupérer le département passé en paramètre
$departement = $_GET['departement'] ?? null;

$villes = [];
if ($departement) {
    $villes = getVilles($departement);
    echo json_encode(['villes' => $villes]);
} else {
    echo json_encode(['error' => 'Département non trouvé']);
}
?>
