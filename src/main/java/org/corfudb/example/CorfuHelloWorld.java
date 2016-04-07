package org.corfudb.example;

import org.docopt.Docopt;

import java.util.Map;
import java.util.UUID;
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
        System.exit(0);
    }

}

