<?php
declare(strict_types=1);
$pageTitle = 'SkyTrack - Statistique';
include "./include/header.inc.php"; 

// Récupération des données du fichier CSV
$log_file = __DIR__ . "/data/consultations.csv";

// Vérifier si le fichier existe
$consultations = [];
if (file_exists($log_file)) {
    // Lire le fichier CSV et extraire les villes
    $file_content = file_get_contents($log_file);
    $lines = explode(PHP_EOL, $file_content);

    foreach ($lines as $line) {
        $data = str_getcsv($line, ';'); // Séparer les données par ';'
        if (count($data) == 2) { // Vérifier qu'il y a bien deux éléments (ville et date)
            $consultations[] = $data[0]; // Ville
        }
    }
}

// Compter le nombre d'occurrences de chaque ville
$city_count = array_count_values($consultations);

// Trier les villes par ordre décroissant de fréquence
arsort($city_count);

// Préparer les données pour le graphique
$labels = array_keys($city_count); // Villes
$values = array_values($city_count); // Nombre de consultations
?>


    <section>
        <!-- Graphique à barres -->
        <canvas id="cityChart" width="400" height="200"></canvas>
        <script>
            const ctx = document.getElementById('cityChart').getContext('2d');
            const cityChart = new Chart(ctx, {
                type: 'bar', // Type du graphique
                data: {
                    labels: <?php echo json_encode($labels); ?>, // Villes
                    datasets: [{
                        label: 'Nombre de consultations',
                        data: <?php echo json_encode($values); ?>, // Nombre de consultations
                        backgroundColor: 'rgba(75, 192, 192, 0.2)',
                        borderColor: 'rgba(75, 192, 192, 1)',
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                    scales: {
                        y: {
                            beginAtZero: true
                        }
                    }
                }
            });
        </script>
    </section>

<?php 
    require "./include/footer.inc.php"; 
?>
