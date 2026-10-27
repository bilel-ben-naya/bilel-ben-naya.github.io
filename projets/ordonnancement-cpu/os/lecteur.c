#include <stdio.h>  
#include <stdlib.h>  
#include "lecteur.h"


Processus *lire_fichier(const char *nom_fichier, int *nb_processus) {


    /* on ouvre le fichier en mode lecture fopen retourne NULL si le fichier n'existe pas */
    FILE *fichier = fopen(nom_fichier, "r");


    /* si le fichier n'existe pas on arrête avec un message d'erreur */
    if (fichier == NULL) {
        printf("Erreur : impossible d'ouvrir %s\n", nom_fichier);
        exit(1);
    }


    /* on réserve de la mémoire pour 100 processus max*/
    Processus *liste = malloc(100 * sizeof(Processus));
    *nb_processus = 0;
     char ligne[256];


    while (fgets(ligne, sizeof(ligne), fichier)) {


        /* on ignore les commentaires et les lignes vides */
        if (ligne[0] == '#' || ligne[0] == '\n')
            continue;


        /* on crée un processus temporaire qui va recevoir les valeurs */
        Processus p;
        int type_es_lu = 0;


        sscanf(ligne, "%d %d %d %d %d %d",
            &p.pid,          
            &p.temps_arrivee,
            &p.duree_cpu,    
            &p.duree_es,      
            &type_es_lu,      
            &p.debut_es);    


        /* on convertit l'entier lu  vers le type énuméré car on a défini type énuméré dans processus.h */
        p.type_es = (TypeES)type_es_lu;


        /* on initialise tous les champs calculés à zéropuis ils seront remplis par fifo_run() pendant la simulation */
        p.temps_restant     = p.duree_cpu; /* au départ il reste tout à faire */
        p.fin_es            = 0;           /* pas encore d'E/S déclenchée */
        p.etat              = PRET;        /* il attend le CPU */
        p.temps_debut       = -1;          /* -1 = pas encore démarré */
        p.temps_fin         = 0;
        p.temps_attente     = 0;
        p.temps_reponse     = 0;
        p.temps_restitution = 0;


        /* on ajoute le processus dans le tableaupuis on incrémente le compteur */
        liste[*nb_processus] = p;
        (*nb_processus)++;
    }


   
    fclose(fichier);
    return liste;
}
