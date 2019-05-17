#ifndef __CLIENT_H
#define __CLIENT_H

#include <graal_isolate.h>


#if defined(__cplusplus)
extern "C" {
#endif

int create_instance(graal_isolatethread_t*);

int ping(graal_isolatethread_t*, int, my_data*);

int run_main(int paramArgc, char** paramArgv);

#if defined(__cplusplus)
}
#endif
#endif
