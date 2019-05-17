package com.labx.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.c.CContext;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.struct.CField;
import org.graalvm.nativeimage.c.struct.CStruct;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.word.PointerBase;
import org.graalvm.word.WordFactory;

class ApiDirective implements CContext.Directives {

    public List<String> getOptions() {
        return Arrays.asList("-I/usr/local/include");
    }

    public List<String> getHeaderFiles() {
        return Collections.singletonList("<poc/simple_data.h>");
    }
}

@CContext(ApiDirective.class)
public class CInterface {
    @CStruct("simple_data")
    interface SimpleData extends PointerBase {
        @CField("str")
        CCharPointer getStr();

        @CField("str")
        void setStr(CCharPointer value);

        @CField("len")
        int getLen();

        @CField("len")
        void setLen(int len);
    }

    private static ConcurrentHashMap<Integer, EchoClient> clients = new ConcurrentHashMap<Integer, EchoClient>();

    private static AtomicInteger INDEX = new AtomicInteger(0);


    @CEntryPoint(name = "create_instance")
    public static int createInstance(IsolateThread thread) {
        EchoClient client = new EchoClient();
        int index = INDEX.getAndIncrement();
        clients.put(index, client);
        return index;
    }


    @CEntryPoint(name = "ping")
    public static int ping(IsolateThread thread, int instanceId, SimpleData simpleData) {
        EchoClient client = clients.get(instanceId);
        try {
            if (null != client) {
                String content = CTypeConversion.toJavaString(simpleData.getStr(), WordFactory.unsigned(simpleData.getLen()));
                client.ping(content);
            }
            return 0;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void main(String[] args) {

        char data[] = new char[1024];
        Arrays.fill(data, 'x');
        String content = new String(data);

        EchoClient client = new EchoClient();

        while (true) {
            try {
                client.ping(content);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
