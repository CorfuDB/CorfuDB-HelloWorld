package org.corfudb.example;

import com.google.common.collect.ImmutableMap;
import org.corfudb.example.airline.AirlineManager;
import org.corfudb.example.airline.objects.Aircraft;
import org.corfudb.example.airline.objects.Airplane;
import org.corfudb.example.airline.objects.DailyFlightSchedule;
import org.corfudb.example.airline.objects.Flight;
import org.corfudb.runtime.CorfuRuntime;
import org.docopt.Docopt;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.fusesource.jansi.Ansi.ansi;

/** This example program exercises the sample airline object class hierarchy we've created. */
public class AirlineExample {

    // This example uses docopt to simplify argument processing. This document below
    // uses the docopt DSL and defines all the valid arguments to invoke the example
    // program. It also sets reasonable defaults. See http://docopt.org for more details.
    private static final String doc =
            "airline, an example airline database on top of Corfu.\n\n"
                    +"Usage:\n"
                    +"  airline view-database [-c <corfu-config>] [-d <db-name>]\n"
                    +"  airline add-plane <aircraft> <tail-number> [-c <corfu-config>] [-d <db-name>]\n"
                    +"  airline add-flight <tail-number> <origin> <dep-datetime> <destination> <arr-datetime> [-c <corfu-config>] [-d <db-name>]\n"
                    +"  airline (-h | --help)\n"
                    +"  airline --version\n\n"
                    +"Options:\n"
                    +"  -c <corfu-config>                   The configuration string for Corfu. [default: localhost:9000]\n"
                    +"  -d <db-name>                        The name of the database to use.    [default: testdb]\n"
                    +"  --h --help                          show this screen\n"
                    +"  --version                           show version.\n"
                    +"Arguments:\n"
                    +"  <dep-datetime>, <arr-datetime>      A datetime in ISO-8601 format e.g. 2016-08-10T11:30:00.\n";

    // This functional interface defines the valid "applets" which our application supports
    @FunctionalInterface
    private interface ExampleFunction {
        /** Run the example applet.
         * @param args  The options map supplied by docopt.
         */
        void run(Map<String, Object> args);
    }

    // This map supplies function pointers to the applet to be run.
    private static Map<String, ExampleFunction> functionMap = ImmutableMap.<String,ExampleFunction>builder()
            .put("view-database", AirlineExample::viewDatabase)
            .put("add-plane", AirlineExample::addPlane)
            .put("add-flight", AirlineExample::addFlight)
            .build();

    // The main entry point to this example program.
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {

        // Install jansi, enabling pretty print capabilities.
        AnsiConsole.systemInstall();

        // Use docopt to parse the command line arguments.
        Map<String, Object> opts = new Docopt(doc).parse(args);

        // Find the name of the applet to run.
        Optional<String> appletToRun = functionMap.keySet().stream()
                                        .filter(x -> opts.containsKey(x) && opts.get(x) instanceof Boolean
                                                    && (Boolean)opts.get(x))
                                        .findFirst();

        if (!appletToRun.isPresent()) {
            System.out.println("Unknown applet given.");
            System.exit(64);
        }

        // Describe what we're about to run:
        System.out.println(ansi().a(appletToRun.get()).a(" using db ").fg(Ansi.Color.GREEN).a(opts.get("-d"))
                    .reset().a(" and runtime args ").fg(Ansi.Color.RED).a(opts.get("-c")).reset());

        // Run the applet.
        functionMap.get(appletToRun.get()).run(opts);
    }

    /** Given a docopt parsed option map, get a CorfuRuntime and connect.
     *
     * @param opts  The docopt option map.
     * @return      A connected CorfuRuntime.
     */
    private static CorfuRuntime getRuntimeAndConnect(Map<String,Object> opts) {
        return new CorfuRuntime((String)opts.get("-c"))
                        .connect();
    }

    /** Given a runtime and a docopt parsed option map, return an airline manager.
     *
     * @param runtime   A connected CorfuRuntime.
     * @param opts      The docopt option map.
     * @return          An AirlineManager.
     */
    private static AirlineManager getAirlineManager(CorfuRuntime runtime, Map<String, Object> opts) {
        return new AirlineManager(runtime, (String) opts.get("-d"));
    }

    /** View the entire database.
     *
     * @param opts  The docopt option map.
     */
    private static void viewDatabase(Map<String, Object> opts) {
        // Get a CorfuRuntime and an AirlineManager to work with.
        CorfuRuntime runtime = getRuntimeAndConnect(opts);
        AirlineManager manager = getAirlineManager(runtime, opts);

        // Print the planes in the system out.
        System.out.println(ansi().fg(Ansi.Color.WHITE).a("Planes:").reset());
        for (Airplane plane : manager.airplanes.getAllAirplanes()) {
            System.out.println(plane.toString());
        }
        System.out.println();

        // Print the flight schedules out.
        System.out.println(ansi().fg(Ansi.Color.WHITE).a("Flight schedules:").reset());
        for (DailyFlightSchedule schedule : manager.schedules.getAllFlightSchedules()) {
            System.out.println(ansi().a("Schedule for ").fg(Ansi.Color.GREEN).a(schedule.getDate()).reset());
            for (Flight f : schedule.getAllFlights()){
                System.out.println(f.toString());
            }
        }
    }

    /** Get the aircraft type from a string.
     *
     * @param aircraftString    A string matching the Aircraft enum.
     * @return                  An Aircraft for the string, or null, if there was no match.
     */
    private static Aircraft parseAircraft(String aircraftString) {
        try {
            return Aircraft.valueOf(aircraftString);
        }
        catch (Exception e) {
            System.out.println("Invalid aircraft type " + aircraftString + " specified. Valid types are: ");
            String types = Arrays.stream(Aircraft.values())
                    .map(Aircraft::toString)
                    .collect(Collectors.joining(", "));
            System.out.println(types);
            return null;
        }

    }

    /** Add a plane to the database.
     *
     * @param opts  The docopt option map.
     */
    private static void addPlane(Map<String, Object> opts) {
        // Get an aircraft type from the string given.
        Aircraft aircraft = parseAircraft((String) opts.get("<aircraft>"));
        if (aircraft == null) return;

        // Connect to the runtime, get an AirlineManager and add the plane to the system.
        // If a plane with this tail number already exists, we'll throw an exception.
        CorfuRuntime runtime = getRuntimeAndConnect(opts);
        AirlineManager manager = getAirlineManager(runtime, opts);
        Airplane p = manager.airplanes.addPlane((String) opts.get("<tail-number>"), aircraft);

        System.out.println(ansi().a("Airplane ").a(p).a(" added."));
    }

    /** Add a flight to the database.
     *
     * @param opts  The docopt option map.
     */
    private static void addFlight(Map<String, Object> opts) {
        // Connect to the runtime, get an AirlineManager and add the flight to the system.
        CorfuRuntime runtime = getRuntimeAndConnect(opts);
        AirlineManager manager = getAirlineManager(runtime, opts);

        // Get the plane we are going to use.
        Airplane p = manager.airplanes.getPlaneByTailNumber((String)opts.get("<tail-number>"));
        if (p == null) {
            System.out.println("Couldn't find a plane with tail number " + opts.get("<tail-number>"));
            return;
        }

        // Try to add the flight to the schedule.
        Flight f = manager.schedules.addNewFlightToSchedule(p, (String)opts.get("<origin>"),
                LocalDateTime.parse((String) opts.get("<dep-datetime>")),
                        (String)opts.get("<destination>"),
                        LocalDateTime.parse((String) opts.get("<arr-datetime>")));

        // The flight is null if there was an error adding the flight (because the plane was already
        // flying somewhere else!)
        if (f == null) {
            System.out.println(ansi().a("Plane ").fg(Ansi.Color.RED).a(p).reset()
                    .a(" is already in use during this timeframe!"));
        }
        else {
            System.out.println(ansi().a("Flight ").a(f).a(" added"));
        }
    }


}

