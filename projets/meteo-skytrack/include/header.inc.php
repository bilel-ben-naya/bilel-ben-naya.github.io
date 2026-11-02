<?php
declare(strict_types=1);

// Initialisation des variables pour éviter les erreurs
$pageTitle = $pageTitle ?? "SkyTrack";
$logoPath = $logoPath ?? "images/Logo.png";
$lang = $_GET['lang'] ?? 'fr';

// Vérifier le cookie pour le style
$style = isset($_COOKIE['style']) ? $_COOKIE['style'] : ($_GET['style'] ?? 'standard');
$style = $style === "alternatif" ? "styles_alt.css" : "styles.css";
?>

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?= htmlspecialchars($pageTitle) ?></title>
    <link rel="icon" href="images/favicon.ico" type="image/x-icon">
    <link rel="stylesheet" href="<?= htmlspecialchars($style) ?>">
</head>
<body class="<?= $style === 'styles_alt.css' ? 'night-mode' : 'day-mode' ?>">

    <header>
        <img src="<?= htmlspecialchars($logoPath) ?>" alt="Logo" id="logo">
        <h1><?= htmlspecialchars($pageTitle) ?></h1>

        <!-- Bouton unique pour changer de mode -->
        <div class="mode-toggle" onclick="toggleMode()">
            <?= $style === 'styles_alt.css' ? '☀️' : '🌙' ?>
        </div>
    </header>

    <div class="menu-container">
        <table class="menu-table">
            <tr>
                <?php 
                $pages = ["index" => "🏠 Accueil", "map" => "📍🗺️ Map", "stats" => "📈 Statistique"];
                foreach ($pages as $page => $label) : ?>
                    <td><a href="<?= $page ?>.php?style=<?= htmlspecialchars($_GET['style'] ?? 'standard') ?>&lang=<?= htmlspecialchars($lang) ?>"><?= $label ?></a></td>
                <?php endforeach; ?>
            </tr>
        </table>
    </div>

    <script>
        function toggleMode() {
            const body = document.body;
            const currentStyle = body.classList.contains('day-mode') ? 'standard' : 'alternatif';
            const newStyle = currentStyle === 'standard' ? 'alternatif' : 'standard';
            const newIcon = currentStyle === 'standard' ? '🌙' : '☀️';
            const currentLang = new URLSearchParams(window.location.search).get('lang');

            // Définir un cookie pour le style choisi
            document.cookie = `style=${newStyle}; path=/; max-age=31536000`; // Le cookie expire dans 1 an

            // Recharger la page avec le nouveau style
            window.location.href = `?style=${newStyle}&lang=${currentLang}`;
        }

        // Changer dynamiquement l'image du header en fonction du mode (avec image aléatoire)
        document.addEventListener('DOMContentLoaded', () => {
            const body = document.body;
            const header = document.querySelector('header');
            
            if (body.classList.contains('night-mode')) {
                // Changer l'image aléatoirement parmi les trois images nuit
                const imagesNuit = ['images/img1-nuit.jpg', 'images/img2-nuit.jpg', 'images/img3-nuit.jpg'];
                const randomImage = imagesNuit[Math.floor(Math.random() * imagesNuit.length)];
                header.style.backgroundImage = `url('${randomImage}')`;
            } else {
                // Changer l'image aléatoirement parmi les trois images jour
                const imagesJour = ['images/img1-jour.jpg', 'images/img2-jour.jpg', 'images/img3-jour.jpg'];
                const randomImage = imagesJour[Math.floor(Math.random() * imagesJour.length)];
                header.style.backgroundImage = `url('${randomImage}')`;
            }
        });
    </script>

</body>
</html>
