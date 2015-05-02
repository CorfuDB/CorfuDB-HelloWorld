package org.corfudb.example;

import org.corfudb.runtime.CorfuDBRuntime;
import org.corfudb.runtime.collections.CDBSimpleMap;
import org.corfudb.runtime.entries.IStreamEntry;
import org.corfudb.runtime.smr.ISMREngineCommand;
import org.corfudb.runtime.smr.SimpleSMREngine;
import org.corfudb.runtime.smr.Stream;
import org.corfudb.runtime.stream.ILog;
import org.corfudb.runtime.stream.IStream;
import org.corfudb.runtime.stream.ITimestamp;
import org.corfudb.runtime.stream.SimpleStream;
import org.corfudb.runtime.view.IConfigurationMaster;
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

        /* Now that we have used a stream, we can now build basic state machines. Fortunately, CorfuDB
         * provides several classes to make this easier. Let's start by using the SimpleSMREngine
         * to create a distributed counter.
         *
         * There is a one-to-one correspondence between SMR engines and streams. That is, each stream
         * contains exactly one SMR engine, and each SMR engine is contained in exactly one stream.
         *
         * SimpleSMREngine takes the object type that it is supposed to wrap around as a type parameter.
         * This type must provide a default, parameterless constructor by default.
         */

        configMaster.resetAll();
        IStream stream4 = cdbFactory.getStream(UUID.randomUUID(), sequencer, addressSpace);
        SimpleSMREngine<AtomicInteger> smr = new SimpleSMREngine<AtomicInteger>(stream4, AtomicInteger.class);

        /* We can provide commands to the SMR engine as lambdas. This lambda is a BiConsumer, which takes the
         * object the command is to act on (in this case, an AtomicInteger), and an options object.
         *
         * Don't forget to cast the lambda into a ISMREngineCommand<T>, otherwise the lambda won't be serializable
         * and your object won't work!
         *
         * We define some basic operations for this counter, increment and decrement.
         */

        ISMREngineCommand<AtomicInteger> increment = (ISMREngineCommand<AtomicInteger>) (a,opt) -> a.getAndIncrement();

        /* We can also define commands that return a value. Since this value will only be resolved on playback,
         * we use a completable future, which can be accessed by calling getReturnResult() on the options object.
         */
        ISMREngineCommand<AtomicInteger> getAndIncrement =
                (ISMREngineCommand<AtomicInteger>) (a,opt) -> {opt.getReturnResult().complete(a.getAndIncrement());};

        /* Now we can propose commands to the object, which will be played back by all clients playing back
         * this object.
         */
        ITimestamp ts3 = smr.propose(increment, null);

        /* To playback a proposal, we call sync to bring the object up to date.
         * We can retrieve the object from the smr engine to get its value.
         */
        smr.sync(ts3);
        System.out.println("Object is now at " +  smr.getObject());

        /* To use an accessor+mutator, we first create a completable future which will contain the result.
         */
        CompletableFuture<Object> previous = new CompletableFuture<Object>();
        ITimestamp ts4 = smr.propose(getAndIncrement, previous);

        /* We can access the result only when the future has completed. See the completable future API for details,
         * but for synchronous inspection, we can join() on the future, which will block the thread until the
         * SMR has synced.
         */
        smr.sync(ts4);
        System.out.println("Object was at " + previous.join());
        System.out.println("And object is now at " + smr.getObject());

        /* CorfuDB provides a collection of objects to work with so you don't have to implement your own.
         * For example, here is a simple map which implements the java.util.Map interface.
         */
        configMaster.resetAll();
        UUID mapId = UUID.randomUUID();
        IStream stream5 = new SimpleStream(mapId, sequencer,addressSpace);
        CDBSimpleMap<Integer, Integer> map = new CDBSimpleMap<Integer, Integer>(stream5);
        map.put(10, 100);
        System.out.println("Map key 10 contains value " + map.get(10));

        /* Of course, any client in the system can now access this map.
         * We can create another "map" based on the same stream. Keep in mind
         * streams and objects have a one-to-one mapping, so you'll need to create a new
         * stream (with the same ID) to use this object:
         */
        IStream stream6 = new SimpleStream(mapId, sequencer,addressSpace);
        CDBSimpleMap<Integer, Integer> map2 = new CDBSimpleMap<Integer, Integer>(stream6);
        System.out.println("Map2 key 10 contains value " + map2.get(10));

        System.exit(0);
    }
}

