package org.corfudb.example.airline;

import org.corfudb.example.airline.query.Airplanes;
import org.corfudb.example.airline.query.DailyFlightSchedules;
import org.corfudb.runtime.CorfuRuntime;

/**
 * The AirlineManager manages all the data objects needed to manage the airline.
 *
 * This consists of an Airplanes object and DailyFlightSchedules object, which are
 * used to run queries on the set of objects available to the ailrine.
 * Created by mwei on 4/6/16.
 */
public class AirlineManager {

    /** The CorfuRuntime this manager will run queries against. */
    private CorfuRuntime runtime;
    /** The name of the database this manager runs under. All streams are prefixed with this name. */
    private String databaseName;

    /** An AirlineManager manages all objects used by the airline.
     *
     * @param runtime       The runtime to execute queries against.
     * @param databaseName  The database name to use. All streams will be prefixed with this name.
     */
    public AirlineManager (CorfuRuntime runtime, String databaseName) {
        this.runtime = runtime;
        this.databaseName = databaseName;

        airplanes = new Airplanes(runtime, databaseName);
        schedules = new DailyFlightSchedules(runtime, databaseName);
    }

    /** Run queries against the airplanes in the system. */
    public Airplanes airplanes;
    /** Run queries against the flight schedule. */
    public DailyFlightSchedules schedules;

}
