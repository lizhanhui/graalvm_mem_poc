#include <iostream>
#include <thread>
#include <vector>
#include "poc/data.h"
#include "client.h"

graal_isolate_t *isolate = nullptr;

void sig_handler(int sig_num) {
  if (sig_num == SIGUSR1) {
    graal_isolatethread_t *thread = graal_get_current_thread(isolate);
    bool need_detachment = false;
    if (thread == nullptr) {
      if (graal_attach_thread(isolate, &thread)) {
        std::cout << "Failed to attach" << std::endl;
        return;
      }
      need_detachment = true;
    }
    dump_heap(thread);
    if (need_detachment) {
      if (graal_detach_thread(thread)) {
        std::cout << "Failed to detach" << std::endl;
      }
    }
  }
}

int main(int argc, char **argv) {
    graal_isolatethread_t *thread = nullptr;

  if (signal(SIGUSR1, sig_handler) == SIG_ERR) {
    std::cout << "Main: failed to register signal handler" << std::endl;
  }

    if (graal_create_isolate(nullptr, &isolate, &thread)) {
        std::cout << "Failed to create isolate and thread" << std::endl;
        return -1;
    }

    int instance_id = create_instance(thread);

    int data_len = 1024;
    char data[data_len + 1];
    for (int i = 0; i < data_len; i++) {
        data[i] = 'x';
    }
    data[data_len] = '\0';

    my_data content = {
        .f_str = data,
        .len = data_len
    };

    while(true) {
        if (ping(thread, instance_id, &content)) {
            std::cout << "Ping failed" << std::endl;
        }
    }

    return 0;
}
