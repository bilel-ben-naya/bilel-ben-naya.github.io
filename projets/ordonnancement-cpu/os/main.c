#include <stdio.h>
#include <stdlib.h>
#include "scheduler.h"


int main() {
   FILE *file = fopen("processus.txt", "r");
   if (!file) {
       printf("Erreur d'ouverture du fichier\n");
       return 1;
   }


     Processus processes[10]; // Créer un tableau pour 10 processus
   int n = 0, id, cpu_time, io_time;


  while (fscanf(file, "%d %d %d", &id, &cpu_time, &io_time) == 3) {
       init_process(&processes[n], id, cpu_time, io_time);
       n++;
   }


   fclose(file);


   // Tester FIFO
   fifo_scheduler(processes, n);


   // Tester SJF
   sjf_scheduler(processes, n);


   // Tester Round Robin avec un quantum de 3ms
   int quantum = 3;
   round_robin_scheduler(processes, n, quantum);
   return 0;
}

