#ifndef __CLIENT_H
#define __CLIENT_H

#include <graal_isolate_dynamic.h>


#if defined(__cplusplus)
extern "C" {
#endif

typedef int (*create_instance_fn_t)(graal_isolatethread_t*);

typedef int (*ping_fn_t)(graal_isolatethread_t*, int, my_data*);

typedef int (*run_main_fn_t)(int paramArgc, char** paramArgv);

#if defined(__cplusplus)
}
#endif
#endif
