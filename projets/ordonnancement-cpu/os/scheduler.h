// scheduler.h
#ifndef SCHEDULER_H
#define SCHEDULER_H


typedef struct {
   int id;         // Identifiant du processus
   int cpu_time;   // Temps de CPU
   int io_time;    // Temps d'E/S
   int wait_time;  // Temps d'attente
   int turnaround_time; // Temps de turnaround
} Processus;


void init_process(Processus *p, int id, int cpu_time, int io_time);


void fifo_scheduler(Processus *processes, int n);


void sjf_scheduler(Processus *processes, int n);


void sjrf_scheduler(Processus *processes, int n); //SRTF


void round_robin_scheduler(Processus *processes, int n, int quantum);


double calculate_cpu_utilization(Processus *processes, int n);


#endif

