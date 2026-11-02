<?php
// Inclure le fichier de fonctions pour accéder à la fonction getHitCount()
require_once 'include/functions.inc.php';
?>

<footer>
    <div class="footer-container">
        <!-- Aide ? Section -->
        <div class="footer-section">
            <h3>Aide ?</h3>
            <ul>
                <li><a href="propos.php">À propos de nous</a></li>
                <li>Réalisé par Alexis SADE - Bilel BEN NAYA ®</li>
            </ul>
        </div>

        <!-- Informations Section -->
        <div class="footer-section">
            <h3>Informations</h3>
            <ul>
                <li><a href="tech.php">Tech</a></li>
                <li><a href="plan.php">Plan du Site</a></li>
                <li>Hits : <?= getHitCount(); ?></li> <!-- Affiche le nombre de hits -->
            </ul>
        </div>

        <!-- Organisme Section -->
        <div class="footer-section">
            <h3>Organisme</h3>
            <ul>
                <li>CY Cergy Paris Université © 2025</li>
            </ul>
        </div>
    </div>
</footer>
