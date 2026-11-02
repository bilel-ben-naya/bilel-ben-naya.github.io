<?php
require_once "include/functions.inc.php";

header("Content-Type: application/json");

$response = [];

// Gestion de la récupération des départements à partir d'une région
if (isset($_GET['region'])) {
    $region = $_GET['region'];
    $departements = getDepartements($region);
    $response['departements'] = array_values($departements); // nettoie les clés
}

// Gestion de la récupération des villes à partir d’un département
if (isset($_GET['departement'])) {
    $departement = $_GET['departement'];
    $villes = getVilles($departement);
    $response['villes'] = array_values($villes); // nettoie les clés
}

echo json_encode($response);
exit;