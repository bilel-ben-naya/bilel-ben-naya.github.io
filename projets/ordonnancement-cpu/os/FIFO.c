#include <stdlib.h>
#include "fifo.h"


void fifo_run(Processus *procs, int n) {


    int horloge    = 0;
    int termines   = 0; /* nombre de processus terminés */


    /* étape 1 : on tri les processus par ordre d'arrivée */
    for (int i = 0; i < n - 1; i++)
        for (int j = 0; j < n - i - 1; j++)
            if (procs[j].temps_arrivee > procs[j+1].temps_arrivee) {
                Processus tmp = procs[j];
                procs[j]      = procs[j+1];
                procs[j+1]    = tmp;
            }


    /* initialisation : tous les processus sont PRET */
    for (int i = 0; i < n; i++)
        procs[i].etat = PRET;


    /*  étape 2 : simulation  */
    while (termines < n) {


        /* on cherche le premier processus PRET dans la file */
        int elu = -1;
        for (int i = 0; i < n; i++) {
            if (procs[i].etat == PRET &&
                procs[i].temps_arrivee <= horloge) {
                elu = i;
                break; /* FIFO = on prend le premier trouvé */
            }
        }


        /* aucun processus prêt : on avance l'horloge */
        if (elu == -1) {


            /* on vérifie si un processus EN_ATTENTE a fini son E/S */
            int prochain_retour = -1;
            for (int i = 0; i < n; i++) {
                if (procs[i].etat == EN_ATTENTE) {
                    if (prochain_retour == -1 ||
                        procs[i].fin_es < procs[prochain_retour].fin_es)
                        prochain_retour = i;
                }
            }


            if (prochain_retour != -1)
                horloge = procs[prochain_retour].fin_es; /* on saute à la fin de l'E/S */
            else
                horloge++; /* on avance d'un ms */


            /* les processus dont l'E/S est terminée repassent PRET */
            for (int i = 0; i < n; i++)
                if (procs[i].etat == EN_ATTENTE &&
                    procs[i].fin_es <= horloge)
                    procs[i].etat = PRET;


            continue;
        }


        /* le processus élu prend le CPU  */
        if (procs[elu].temps_debut == 0 && procs[elu].temps_arrivee == horloge)
            procs[elu].temps_debut = horloge;


        /* premier démarrage du processus */
        if (procs[elu].temps_reponse == -1 || procs[elu].temps_debut == horloge)
            procs[elu].temps_reponse = horloge - procs[elu].temps_arrivee;


        procs[elu].etat = EN_COURS;


        /* le processus doit-il faire une E/S ? */
        if (procs[elu].duree_es > 0 &&
            procs[elu].temps_restant == procs[elu].duree_cpu - procs[elu].debut_es) {


            /* il a calculé jusqu'à debut_es  il déclenche l'E/S */
            procs[elu].etat   = EN_ATTENTE;
            procs[elu].fin_es = horloge + procs[elu].duree_es;


            /* pour E/S parallélisée : le CPU est libre donc autre processus peut tourner */
            /* pour E/S non parallélisable : le CPU est bloqué jusqu'à fin_es */
            if (procs[elu].type_es == ES_NON_PARALLELE)
                horloge = procs[elu].fin_es;


        } else if (procs[elu].temps_restant > 0) {


            /* le processus calcule pendant 1 ms */
            procs[elu].temps_restant--;
            horloge++;


            /* si c'est fini */
            if (procs[elu].temps_restant == 0) {
                procs[elu].temps_fin         = horloge;
                procs[elu].temps_restitution = horloge - procs[elu].temps_arrivee;
                procs[elu].temps_attente     = procs[elu].temps_restitution
                                               - procs[elu].duree_cpu
                                               - procs[elu].duree_es;
                procs[elu].etat              = TERMINE;
                termines++;
            }
        }


        /* les processus dont l'E/S est terminée repassent PRET */
        for (int i = 0; i < n; i++)
            if (procs[i].etat == EN_ATTENTE &&
                procs[i].fin_es <= horloge)
                procs[i].etat = PRET;
    }
}
