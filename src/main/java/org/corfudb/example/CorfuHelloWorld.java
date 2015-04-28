package org.corfudb.example;

import org.corfudb.client.CorfuDBClient;
import org.corfudb.client.view.Sequencer;
import org.corfudb.client.view.WriteOnceAddressSpace;
import org.corfudb.client.abstractions.SharedLog;

import org.corfudb.client.OutOfSpaceException;

/**
 * Created by dmalkhi on 1/16/15.
 */
public class CorfuHelloWorld {
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {

        String masteraddress = null;

        if (args.length >= 1) {
            masteraddress = args[0]; // TODO check arg.length
        } else {
            // throw new Exception("must provide master http address"); // TODO
            masteraddress = "http://localhost:8002/corfu";
        }

        final int numthreads = 1;

        CorfuDBClient client = new CorfuDBClient(masteraddress);
        Sequencer s = new Sequencer(client);
        WriteOnceAddressSpace woas = new WriteOnceAddressSpace(client);
        SharedLog sl = new SharedLog(s, woas);
        client.startViewManager();

        System.out.println("Appending hello world into log...");
        long address = 0;
        try {
            address = sl.append("hello world".getBytes());
        }
        catch (OutOfSpaceException oose)
        {
            System.err.println("Out of space during append!");
            System.exit(1);
        }
        System.out.println("Successfully appended hello world into log position " + address);
        System.out.println("Reading back entry at address " + address);
        byte[] result = sl.read(address);
        System.out.println("Readback complete, result size=" + result.length);
        String sresult = new String(result, "UTF-8");
        System.out.println("Contents were: " + sresult);
        if (!sresult.toString().equals("hello world"))
                {
                    System.out.println("ASSERT Failed: String did not match!");
                    System.exit(1);
                }

        System.out.println("Successfully completed test!");
        System.exit(0);

    }
}

