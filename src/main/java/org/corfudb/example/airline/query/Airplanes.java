package org.corfudb.example.airline.query;

import org.corfudb.example.airline.objects.Aircraft;
import org.corfudb.example.airline.objects.Airplane;
import org.corfudb.runtime.CorfuRuntime;
import org.corfudb.runtime.collections.FGMap;
import org.corfudb.runtime.view.ObjectOpenOptions;

import java.util.EnumSet;

/**
 * Created by mwei on 4/6/16.
 */
public class Airplanes {

    private FGMap<String, Airplane> airplaneMap;
    private CorfuRuntime runtime;
    private String databaseName;

    @SuppressWarnings("unchecked")
    public Airplanes(CorfuRuntime runtime, String databaseName) {
        airplaneMap = runtime.getObjectsView().open(databaseName + "airplanes", FGMap.class);
        this.runtime = runtime;
        this.databaseName = databaseName;
    }

    public Airplane addPlane(String tailNumber, Aircraft type) {
        Airplane ap = runtime.getObjectsView().build()
                                .setStreamName(databaseName + "airplane" + tailNumber)
                                .setType(Airplane.class)
                                .addOption(ObjectOpenOptions.CREATE_ONLY)
                                .setArguments(tailNumber, type)
                                .open();
        airplaneMap.put(ap.tailNumber, ap);
        return ap;
    }

    public Airplane getPlaneByTailNumber(String tailNumber) {
        return airplaneMap.get(tailNumber);
    }
}
