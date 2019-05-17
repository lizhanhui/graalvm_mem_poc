#include <iostream>
#include <thread>
#include <vector>
#include "poc/data.h"
#include "client.h"

int main(int argc, char **argv) {
    graal_isolate_t *isolate = nullptr;
    graal_isolatethread_t *thread = nullptr;

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
