#ifndef PROCESSUS_H
#define PROCESSUS_H


/* Les 4 états possibles d'un processus  */
typedef enum {
    PRET,           /* le processus est arrivé et attend le CPU */
    EN_COURS,       /* le processus tourne actuellement sur le CPU */
    EN_ATTENTE,     /* le processus est bloqué sur une E/S */
    TERMINE         /* le processus a fini son travail */
} Etat;


/* Les 3 types d'E/S possibles */
typedef enum {
    ES_AUCUNE        = 0,  /* pas d'E/S du tout */
    ES_PARALLELE     = 1,  /* CPU libre pendant l'E/S donc un autre peut tourner */
    ES_NON_PARALLELE = 2   /* CPU bloqué pendant l'E/S donc tout  le monde attend */
} TypeES;


/* Structure représentant les informations  du processus*/
typedef struct {


    /* les données d'entrée (lues depuis le fichier) */
    int    pid;              /* identifiant unique du processus */
    int    temps_arrivee;    /* instant auquel le processus arrive (ms) */
    int    duree_cpu;        /* durée totale de calcul CPU nécessaire (ms) */
    int    duree_es;         /* durée de la pause E/S (0 si pas d'E/S) */
    int    debut_es;         /* après combien de ms de CPU l'E/S se déclenche */
    TypeES type_es;          /* type d'E/S : aucune, parallèle, non parallèle */


    /* les données calculées pendant la simulation --- */
    int    temps_restant;    /* temps CPU qu'il reste à exécuter (pour SJRF et RR) */
    int    fin_es;           /* instant auquel l'E/S se termine */
    Etat   etat;             /* état actuel du processus */


    /* les calculs après la simulation --- */
    int    temps_debut;       /* instant où le processus tourne pour la 1ère fois */
    int    temps_fin;         /* instant où le processus se termine */
    int    temps_attente;     /* temps total passé à attendre le CPU */
    int    temps_reponse;     /* temps_debut - temps_arrivee */
    int    temps_restitution; /* temps_fin - temps_arrivee */


} Processus;


#endif


