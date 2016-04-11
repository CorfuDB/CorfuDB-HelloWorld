package org.corfudb.example.airline.query;

import org.corfudb.example.airline.objects.Aircraft;
import org.corfudb.example.airline.objects.Airplane;
import org.corfudb.runtime.CorfuRuntime;
import org.corfudb.runtime.collections.FGMap;
import org.corfudb.runtime.view.ObjectOpenOptions;

import java.util.Collection;
import java.util.EnumSet;

/**
 * This class runs queries against that airplanes in the system.
 * Created by mwei on 4/6/16.
 */
public class Airplanes {

    /** A list of airplanes in the system we know about. */
    private FGMap<String, Airplane> airplaneMap;
    /** The CorfuRuntime to run queries under. */
    private CorfuRuntime runtime;
    /** The name of the database to use. */
    private String databaseName;

    /** Create a new query object backed by a runtime and a databasename.
     *
     * @param runtime       The CorfuRuntime to run queries against.
     * @param databaseName  The name of the database to use.
     */
    @SuppressWarnings("unchecked")
    public Airplanes(CorfuRuntime runtime, String databaseName) {
        airplaneMap = runtime.getObjectsView().open(databaseName + "airplanes", FGMap.class);
        this.runtime = runtime;
        this.databaseName = databaseName;
    }

    /** Add a new plane to the database. If the plane already exists (with this tailNumber)
     * , we fail with a ObjectExistsException.
     *
     * @param tailNumber    The tail number of the plane to use.
     * @param type          The type of plane.
     * @throws org.corfudb.runtime.exceptions.ObjectExistsException     If the object already exists.
     * @return              A new plane, if the plane doesn't already exist.
     */
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

    /** Get a plane using it's tail number.
     *
     * @param tailNumber    The tail number to retrieve.
     * @return              A plane with the tail number, or null, if there was no such plane.
     */
    public Airplane getPlaneByTailNumber(String tailNumber) {
        return airplaneMap.get(tailNumber);
    }

    /** Get all airplanes in the system.
     *
     * @return              A collection containing all airplanes in the system (at the time the query was called).
     */
    public Collection<Airplane> getAllAirplanes() {
        return airplaneMap.values();
    }
}
