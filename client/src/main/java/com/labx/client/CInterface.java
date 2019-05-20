package com.labx.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
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



@CContext(CInterface.ApiDirective.class)
public class CInterface {

    static class ApiDirective implements CContext.Directives {

        public List<String> getOptions() {
            return Arrays.asList("-I/root/include");
        }

        public List<String> getHeaderFiles() {
            return Collections.singletonList("<poc/data.h>");
        }
    }

    @CStruct("my_data")
    interface SimpleData extends PointerBase {
        @CField("f_str")
        CCharPointer getStr();

        @CField("f_str")
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

    private static final String HEAP_DUMP_COMMAND = "HeapDump.dumpHeap(FileOutputStream, Boolean)Boolean";

    /**
     * Generate heap dump and save it into temp file.
     */
    private static void createHeapDump() {
        boolean heapDumpCreated = false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
            String userHome = System.getProperty("user.home");
            File file = new File(new File(userHome), "SVMHeapDump-" + sdf.format(new Date()) + ".hprof");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            // Create heap dump
            final Object[] args = new Object[]{HEAP_DUMP_COMMAND, fileOutputStream, Boolean.TRUE};
            final Object resultObject = Compiler.command(args);
            // Following code checks if heap dump was created using return value
            if (resultObject instanceof Boolean) {
                heapDumpCreated = ((Boolean) resultObject).booleanValue();
            }
            fileOutputStream.close();

            if (heapDumpCreated){
                System.out.println("  Heap dump created " + file.getAbsolutePath() + ", size: " + file.length());
            } else {
                // Delete the file to not pollute disk with empty files.
                System.out.println("  Heap dump creation failed.");
                file.delete();
            }
        } catch (IOException ioe) {
            System.out.println("  Caught IOException.");
        }
    }

    @CEntryPoint(name = "dump_heap")
    public static void dumpHeap(IsolateThread thread) {
        createHeapDump();
    }

    public static void main(String[] args) {

        char data[] = new char[1024];
        Arrays.fill(data, 'x');
        String content = new String(data);

        EchoClient client = new EchoClient();

        for (int i = 0; i < 1000; i++) {
            try {
                client.ping(content);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        client.shutdown();
    }

}
