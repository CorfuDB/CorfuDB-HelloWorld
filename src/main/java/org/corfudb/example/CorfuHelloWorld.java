package org.corfudb.example;

import org.corfudb.runtime.CorfuDBRuntime;
import org.corfudb.runtime.entries.IStreamEntry;
import org.corfudb.runtime.smr.Stream;
import org.corfudb.runtime.stream.ILog;
import org.corfudb.runtime.stream.IStream;
import org.corfudb.runtime.stream.ITimestamp;
import org.corfudb.runtime.view.IConfigurationMaster;
import org.corfudb.runtime.view.IStreamingSequencer;
import org.corfudb.runtime.view.IWriteOnceAddressSpace;
import org.corfudb.util.CorfuDBFactory;
import org.docopt.Docopt;

import java.util.Map;
import java.util.UUID;

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

    public static void main(String[] args) throws Exception {

        /*  This example uses docopt to parse command line arguments.
         *  For more information on how to use docopt, see http://docopt.org
         */
        Map<String,Object> opts = new Docopt(doc).withVersion("git").parse(args);

        /* The convenience class CorfuDBFactory allows us to create
         * CorfuDB class instances based on command line configuration parsed by docopt.
         */
        CorfuDBFactory cdbFactory = new CorfuDBFactory(opts);

        /* To interact with a CorfuDB instance, we first need a runtime to interact with.
         * We can get an instance of CorfuDBRuntime by using the factory.
         */
        CorfuDBRuntime cdr = cdbFactory.getRuntime();

        /* Each CorfuDB instance consists of a configuration master, write once address
         * space and sequencer. We can use the factory to get an instance.
         */
        IWriteOnceAddressSpace addressSpace = cdbFactory.getWriteOnceAddressSpace(cdr);
        IStreamingSequencer sequencer = cdbFactory.getStreamingSequencer(cdr);
        IConfigurationMaster configMaster = cdbFactory.getConfigurationMaster(cdr);

        /* These classes are views over the CorfuDB instance. They automatically reconfigure
         * if the underlying system changes, so you will not have to create a new
         * sequencer for example, if the sequencer fails.
         *
         * The configuration master provides a resetAll command which resets the state
         * of the system. You should not use it in production, but it is very useful for
         * testing purposes.
         */
        configMaster.resetAll();

        /* The sequencer provides incrementing tokens, while the write once address space
         * allows us to write to addresses exactly once. We can read/write randomly
         * to the address space.
         */
        UUID streamId = UUID.randomUUID();
        long token = sequencer.getNext(streamId);
        addressSpace.write(token, "hello world");

        /* If we were to write to the address space at the address given to token again here,
         * we would get an overwrite exception. If we attempted to read an address in that
         * address space before it was written, we would get an unwritten exception.
         */
        Object o = addressSpace.readObject(token);
        System.out.println("Read back the string " + o);

        /* Normally, we'll want something a little nicer to use than an address space. The log
         * class does that, giving us a read, append and trim interface as described in the original
         * CORFU paper.
         */
        configMaster.resetAll();
        ILog log = cdbFactory.getLog(sequencer, addressSpace);
        ITimestamp log_timestamp = log.append("hello world from a log");
        String text = (String) log.read(log_timestamp);
        System.out.println("Read back the string " + text);

        /* In addition to shared logs, we also get streams, which allow us to virtualize a CorfuDB
         * instance, providing multiple log-like interfaces at a time. Unlike logs, however,
         * streams are read forward from the beginning using a readNext() interface.
         *
         * We use UUIDs to uniquely identify streams. You can use the UUID.randomUUID() function
         * to generate globally unique stream identifiers.
         */
        configMaster.resetAll();
        IStream stream1 = cdbFactory.getStream(UUID.randomUUID(), sequencer, addressSpace);
        IStream stream2 = cdbFactory.getStream(UUID.randomUUID(), sequencer, addressSpace);

        stream1.append("hello world from stream 1");
        stream2.append("hello world from stream 2");
        stream1.append("hello world again from stream 1");

        System.out.println("Read back the string " + stream1.readNextObject());
        System.out.println("Read back the string " + stream1.readNextObject());
        System.out.println("Read back the string " + stream2.readNextObject());

        /* Stream entries have timestamps which we can use to compare ordering. Depending on the stream
         * implementation, timestamps may be comparable across streams (you should expect that they will
         * typically be not comparable).
         */
        configMaster.resetAll();
        IStream stream3 = cdbFactory.getStream(UUID.randomUUID(), sequencer, addressSpace);

        stream3.append("hello world from stream 3");
        stream3.append("hello world from stream 4");

        IStreamEntry entry1 = stream3.readNextEntry();
        IStreamEntry entry2 = stream3.readNextEntry();

        ITimestamp ts1 = entry1.getTimestamp();
        ITimestamp ts2 = entry2.getTimestamp();
        System.out.println("Comparison of ts1 to ts2 returns " + ts1.compareTo(ts2));


    }
}

