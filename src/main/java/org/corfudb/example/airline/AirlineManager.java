package org.corfudb.example.airline;

import org.corfudb.example.airline.query.Airplanes;
import org.corfudb.example.airline.query.DailyFlightSchedules;
import org.corfudb.runtime.CorfuRuntime;

/**
 * Created by mwei on 4/6/16.
 */
public class AirlineManager {

    private CorfuRuntime runtime;
    private String databaseName;

    public AirlineManager (CorfuRuntime runtime, String databaseName) {
        this.runtime = runtime;
        this.databaseName = databaseName;

        airplanes = new Airplanes(runtime, databaseName);
        schedules = new DailyFlightSchedules(runtime, databaseName);
    }

    public Airplanes airplanes;
    public DailyFlightSchedules schedules;

}
