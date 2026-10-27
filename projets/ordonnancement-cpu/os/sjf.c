#include "sjf.h"


void sjf_run(Processus *procs, int n) {


    int horloge  = 0;
    int termines = 0;


    /* initialisation : tous les processus sont PRET */
    for (int i = 0; i < n; i++) {
        procs[i].etat        = PRET;
        procs[i].temps_debut = -1; /* -1 = pas encore démarré */
    }


    while (termines < n) {


        /* on cherche le processus PRET avec la plus courte durée CPU  */
        int elu = -1;
        for (int i = 0; i < n; i++) {
            if (procs[i].etat == PRET &&
                procs[i].temps_arrivee <= horloge) {


                /* on garde ce processus si : personne n'a encore été élu (elu == -1) ou il est plus court que l'élu actuel */
                if (elu == -1 ||
                    procs[i].duree_cpu < procs[elu].duree_cpu) {
                    elu = i;
                }
            }
        }


        /* personne n'est prêt : on avance l'horloge */
        if (elu == -1) {
            horloge++;


            /* on vérifie si une E/S vient de se terminer */
            for (int i = 0; i < n; i++)
                if (procs[i].etat == EN_ATTENTE &&
                    procs[i].fin_es <= horloge)
                    procs[i].etat = PRET;


            continue;
        }


        /* premier démarrage du processus */
        if (procs[elu].temps_debut == -1) {
            procs[elu].temps_debut   = horloge;
            procs[elu].temps_reponse = horloge - procs[elu].temps_arrivee;
        }


        procs[elu].etat = EN_COURS;


        /* le processus calcule 1 ms */
        procs[elu].temps_restant--;
        horloge++;


        /* combien de ms de CPU il a déjà fait */
        int cpu_fait = procs[elu].duree_cpu - procs[elu].temps_restant;


        /* on vérifie s’il va déclencher son E/S maintenant  */
        if (procs[elu].duree_es > 0 &&
            procs[elu].fin_es == 0 &&
            cpu_fait == procs[elu].debut_es) {


            /* il libère le CPU et attend la fin de son E/S */
            procs[elu].etat   = EN_ATTENTE;
            procs[elu].fin_es = horloge + procs[elu].duree_es;
        }


        /on vérifie s’il a fini tout son calcul CPU  */
        if (procs[elu].temps_restant == 0) {
            procs[elu].temps_fin         = horloge;
            procs[elu].temps_restitution = horloge - procs[elu].temps_arrivee;
            procs[elu].temps_attente     = procs[elu].temps_restitution
                                           - procs[elu].duree_cpu
                                           - procs[elu].duree_es;
            procs[elu].etat              = TERMINE;
            termines++;
        }


        /* on vérifie si une E/S vient de se terminer */
        for (int i = 0; i < n; i++)
            if (procs[i].etat == EN_ATTENTE &&
                procs[i].fin_es <= horloge)
                procs[i].etat = PRET;
    }
}
