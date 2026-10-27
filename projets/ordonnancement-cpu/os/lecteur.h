#ifndef LECTEUR_H
#define LECTEUR_H


#include "processus.h"


/*
  - nom_fichier  : chemin vers le fichier à lire
  - nb_processus : pointeur rempli avec le nombre de processus lus
 */
Processus *lire_fichier(const char *nom_fichier, int *nb_processus);


#endif



