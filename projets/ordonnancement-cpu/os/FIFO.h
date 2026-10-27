#ifndef FIFO_H
#define FIFO_H


#include "processus.h"


/* - procs : tableau de processus à ordonnancer
   - n     : nombre de processus dans le tableau
 */
void fifo_run(Processus *procs, int n);


#endif
