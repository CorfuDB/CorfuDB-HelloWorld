package org.corfudb.example.org.corfudb.example.counter;

import org.corfudb.runtime.CorfuRuntime;
import org.corfudb.runtime.object.Accessor;
import org.corfudb.runtime.object.ICorfuSMRObject;
import org.corfudb.runtime.object.Mutator;
import org.docopt.Docopt;

import java.util.Map;

/**
 * Created by dmalkhi on 6/28/16.
 */
public class SimpleCounterExample {

    // This example uses docopt to simplify argument processing. This document below
    // uses the docopt DSL and defines all the valid arguments to invoke the example
    // program. It also sets reasonable defaults. See http://docopt.org for more details.
    private static final String doc =
            "a simple counter example over Corfu.\n\n"
                    +"Usage:\n"
                    +"  simplecounter [-c <corfu-config>]\n"
                    +"  simplecounter (-h | --help)\n\n"
                    +"Options:\n"
                    +"  -c <corfu-config>                   The configuration string for Corfu. [default: localhost:9000]\n"
                    +"  --h --help                          show this screen\n";


    CorfuRuntime runtime;

    public SimpleCounterExample(CorfuRuntime runtime) {
        this.runtime = runtime;
    }

    public static class SharedCounter implements ICorfuSMRObject {
        Integer cnt;


        @Accessor
        public Integer getCnt() { return cnt; }

        @Mutator
        public void setCnt(int v) { cnt = v; }

    }

    public SharedCounter getSharedCounter() {
        // create a shared counter of type Intefer with name "SimpleCounterExample"
        SharedCounter SharedCounter = runtime.getObjectsView().build()
                .setType(SharedCounter.class)
                .setStreamName("SimpleCounterExample")
                .open();
        return SharedCounter;
    }

    public static void main(String[] args) throws Exception {
        // Use docopt to parse the command line arguments.
        Map<String, Object> opts = new Docopt(doc).parse(args);
        CorfuRuntime runtime = new CorfuRuntime((String) opts.get("-c"))
                .connect();
        SimpleCounterExample simpleCounterExample = new SimpleCounterExample(runtime);
        SharedCounter cnt = simpleCounterExample.getSharedCounter();
        cnt.setCnt(44);
        assert cnt.getCnt() == 44;
    }
}
