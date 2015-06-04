package org.corfudb.example;

import org.corfudb.runtime.CorfuDBRuntime;
import org.corfudb.runtime.collections.CDBSimpleMap;
import org.corfudb.runtime.entries.IStreamEntry;
import org.corfudb.runtime.smr.ISMREngineCommand;
import org.corfudb.runtime.smr.SimpleSMREngine;
import org.corfudb.runtime.stream.ILog;
import org.corfudb.runtime.stream.IStream;
import org.corfudb.runtime.stream.ITimestamp;
import org.corfudb.runtime.view.IConfigurationMaster;
import org.corfudb.runtime.view.ICorfuDBInstance;
import org.corfudb.runtime.view.IStreamingSequencer;
import org.corfudb.runtime.view.IWriteOnceAddressSpace;
import org.corfudb.util.CorfuDBFactory;
import org.docopt.Docopt;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class CorfuHelloWorld {

    private static final String doc =
            "corfudb_helloworld, the corfudb hello world example app.\n\n"
                    +"Usage:\n"
                    +"  corfudb_helloworld run [-m <master>] [-a <type>]\n"
                    +"  corfudb_helloworld (-h | --help)\n"
                    +"  corfudb_helloworld --version\n\n"
                    +"Options:\n"
                    +"  -m <master>, --master <master>          The address of the configuration master. [default: memory]\n"
                    +"  -a <type> --address-space <type>        The type of address space to use. [default: WriteOnceAddressSpace]\n"
                    +"  --h --help                              show this screen\n"
                    +"  --version                               show version.\n";


    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {

        /*  This example uses docopt to parse command line arguments.
         *  For more information on how to use docopt, see http://docopt.org
         */
        Map<String, Object> opts = new Docopt(doc).withVersion("git").parse(args);

        /* The convenience class CorfuDBFactory allows us to create
         * CorfuDB class instances based on command line configuration parsed by docopt.
         */
        report("Creating CorfuDBFactory...");
        CorfuDBFactory cdbFactory = new CorfuDBFactory(opts);

        /* To interact with a CorfuDB instance, we first need a runtime to interact with.
         * We can get an instance of CorfuDBRuntime by using the factory.
         */
        report("Creating CorfuDBRuntime...");
        CorfuDBRuntime cdr = cdbFactory.getRuntime();

        /* The basic unit of CorfuDB is called an instance. It encapsulates the logging units,
         * configuration master and sequencer. It provides the primary method of interacting with
          * CorfuDB.
          *
          * To get an instance, call the .getLocalInstance() method on CorfuDBRuntime.
         */

        ICorfuDBInstance instance = cdr.getLocalInstance();

        /*
         * The configuration master provides a resetAll command which resets the state
         * of the system. You should not use it in production, but it is very useful for
         * testing purposes.
         */
        report("resetting configuration...");
        instance.getConfigurationMaster().resetAll();


        /* CorfuDB provides a collection of objects to work with so you don't have to implement your own.
         * For example, here is a simple map which implements the java.util.Map interface.
         */
        CDBSimpleMap<Integer, Integer> map =
            (CDBSimpleMap<Integer, Integer>)
                cdr.getLocalInstance().openObject(UUID.randomUUID(), CDBSimpleMap.class);
        map.put(10, 100);
        report("Map key 10 contains value ", map.get(10), 100);

        /* Of course, any client in the system can now access this map.
         */

        System.out.println("DONE!!!");
        System.exit(finalResult());
    }

    protected static void report(String message) {
        System.out.println(message);
    }


    /*  report the result of scenario that exercises a CorfuDB function/feature.
        Accepts a string, the actual result, and the expected value, which is
        assumed to implement Comparable (which certainly the case for the
        simple test scenarios in the hello world sample.)
        Side effects: tracks number of actual tests and failures in inform
                      subsequent summary.
     */
    protected static void report(String message, Object oactual, Comparable expected) {
        Object actual = (oactual instanceof AtomicInteger) ? ((AtomicInteger)oactual).get() : oactual;
        boolean success = expected == null ? actual == null : expected.compareTo(actual) == 0;
        String successstr = success ? " (OK)":" (ERROR)";
        System.out.println(message + " " + actual + successstr);
        failCount += success ? 0 : 1;
        testCount++;
    }

    /*  Emit summary of run. If there were errors, complain about it,
        ensure the process return code reflects that fact.
        Returns: intended process return code (param for exit() syscall);
     */
    protected static int finalResult() {
        if(failCount == 0) {
            System.out.println("ALL TESTS PASSED.");
            return 0;
        }
        System.out.println("ENCOUNTERED ERRORS: "+failCount+" of "+testCount+" failed.");
        return -1;
    }


    /*  Track whether the simple functionality exercises illustrated
     *  in the following codes actually produce the expected results.
     */
    protected static int failCount = 0;
    protected static int testCount = 0;

}

