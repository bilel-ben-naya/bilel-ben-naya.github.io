<?php
    declare(strict_types=1);
    $title = "Plan du site";

    require "./include/header.inc.php";
    require "./include/functions.inc.php";
?>
<main>
        <h1>Plan du Site</h1>
        <section>
            <h2 class="a-plan-b">Sommaire</h2>
            <article>
                <h3><a href="index.php" class="a-plan">Acceuil</a></h3>
            </article>

            <article>
                <h3><a href="map.php" class="a-plan">Météo</a></h3>
            </article>

            <article>
                <h3><a href="tech.php" class="a-plan">Page Tech</a></h3>
            </article>

            <article>
                <h3><a href="stats.php" class="a-plan">Statistiques des Villes les plus consultées</a></h3>
            </article>

            <article>
                <h3><a href="propos.php" class="a-plan">À Propos</a></h3>
                <ul>
                </ul>
            </article>
        </section>
    </main>
<?php
    require "./include/footer.inc.php";
?>