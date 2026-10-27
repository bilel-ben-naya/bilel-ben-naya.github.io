// scheduler.c
#include <stdio.h>
#include <stdlib.h>
#include "scheduler.h"


void init_process(Processus *p, int id, int cpu_time, int io_time) {
   p->id = id;
   p->cpu_time = cpu_time;
   p->io_time = io_time;
   p->wait_time = 0;  // Initialiser les temps d'attente et de turnaround à 0
   p->turnaround_time = 0;
}
void fifo_scheduler(Processus *processes, int n) {
   int total_wait_time = 0, total_turnaround_time = 0;


   for (int i = 0; i < n; i++) {
       processes[i].wait_time = (i == 0) ? 0 : processes[i - 1].wait_time + processes[i - 1].cpu_time;
       processes[i].turnaround_time = processes[i].wait_time + processes[i].cpu_time;


       total_wait_time += processes[i].wait_time;
       total_turnaround_time += processes[i].turnaround_time;


       printf("Processus %d: Attente = %d, Retour = %d\n", processes[i].id, processes[i].wait_time, processes[i].turnaround_time);
   }


   printf("Temps d'attente moyen: %f\n", (float)total_wait_time / n);
   printf("Temps de turnaround moyen: %f\n", (float)total_turnaround_time / n);
}


int compare_cpu_time(const void *a, const void *b) {
   Processus *p1 = (Processus *)a;
   Processus *p2 = (Processus *)b;
   return p1->cpu_time - p2->cpu_time;
}


void sjf_scheduler(Processus *processes, int n) {
   int total_wait_time = 0, total_turnaround_time = 0;


   // Trier les processus par leur temps de CPU
   qsort(processes, n, sizeof(Processus), compare_cpu_time);


   for (int i = 0; i < n; i++) {
       processes[i].wait_time = (i == 0) ? 0 : processes[i - 1].wait_time + processes[i - 1].cpu_time;
       processes[i].turnaround_time = processes[i].wait_time + processes[i].cpu_time;


       total_wait_time += processes[i].wait_time;
       total_turnaround_time += processes[i].turnaround_time;


       printf("Processus %d: Attente = %d, Retour = %d\n", processes[i].id, processes[i].wait_time, processes[i].turnaround_time);
   }


   printf("Temps d'attente moyen: %f\n", (float)total_wait_time / n);
   printf("Temps de turnaround moyen: %f\n", (float)total_turnaround_time / n);
}


void round_robin_scheduler(Processus *processes, int n, int quantum) {
   int total_wait_time = 0, total_turnaround_time = 0;
   int remaining_time[n];


   // Initialiser le temps restant pour chaque processus
   for (int i = 0; i < n; i++) {
       remaining_time[i] = processes[i].cpu_time;
   }


   // Simuler l'exécution des processus
   int all_done = 0;
   while (!all_done) {
       all_done = 1;
       for (int i = 0; i < n; i++) {
           if (remaining_time[i] > 0) {
               all_done = 0;
               if (remaining_time[i] > quantum) {
                   processes[i].wait_time += quantum;
                   remaining_time[i] -= quantum;
               } else {
                   processes[i].wait_time += remaining_time[i];
                   remaining_time[i] = 0;
               }
           }
       }
   }


   // Calculer le temps de turnaround et afficher les résultats
   for (int i = 0; i < n; i++) {
       processes[i].turnaround_time = processes[i].wait_time + processes[i].cpu_time;
       total_wait_time += processes[i].wait_time;
       total_turnaround_time += processes[i].turnaround_time;


       printf("Processus %d: Attente = %d, Retour = %d\n", processes[i].id, processes[i].wait_time, processes[i].turnaround_time);
   }


   printf("Temps d'attente moyen: %f\n", (float)total_wait_time / n);
   printf("Temps de turnaround moyen: %f\n", (float)total_turnaround_time / n);
}
