package org.corfudb.example.airline.objects;

import org.corfudb.runtime.object.*;
import org.corfudb.runtime.view.ObjectOpenOptions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This object contains a flight schedule for a particular day.
 * Created by mwei on 4/7/16.
 */
@CorfuObject(objectType= ObjectType.SMR,
        constructorType= ConstructorType.PERSISTED,
        stateType = DailyFlightSchedule.class,
        stateSource = StateSource.SELF
)
public class DailyFlightSchedule implements ICorfuSMRObject<DailyFlightSchedule> {

    /** The flights for this day. */
    Map<Integer, Flight> dailyFlights = new ConcurrentHashMap<>();
    /** The next flight number to issue. */
    AtomicInteger nextFlightNumber = new AtomicInteger(1);
    /** The name of the database to use. */
    String databaseName;
    /** The date that this flight schedule represents. */
    LocalDate dateTime;

    /** Create a new flight schedule with a certain prefix and date.
     *
     * @param databaseName  The name of the database to use.
     * @param dateTime      The date that this schedule is used for.
     */
    public DailyFlightSchedule(String databaseName, LocalDate dateTime) {
        this.databaseName = databaseName;
        this.dateTime = dateTime;
    }

    /** Get the name used to prefix a stream. This method is not instrumented so it does
     * not go through SMR.
     * @return              The name used to prefix a string.
     */
    @DontInstrument
    public String getStreamPrefix() {
        return databaseName + dateTime.toString();
    }

    /** Get the date which this schedule represents.
     *
     * @return              The date the schedule represents.
     */
    @Accessor
    public LocalDate getDate() {
        return dateTime;
    }

    /** Get all flights for this date. */
    @Accessor
    public Collection<Flight> getAllFlights() {
        return dailyFlights.values();
    }

    /**
     * Add a new flight to this schedule, checking if the given plane is used in any other flight today.
     * @param plane                 The plane which will be used for this flight.
     * @param origin                The location the plane will depart from.
     * @param estimatedDeparture    When the plane is estimated to depart.
     * @param destination           The location the plane will land at.
     * @param estimatedArrival      When the plane is estimated to arrive.
     * @return
     */
    @MutatorAccessor
    public Flight addFlight(Airplane plane, String origin, LocalDateTime estimatedDeparture,
                             String destination, LocalDateTime estimatedArrival)
    {
        // Make sure the plane type was not null.
        if (plane == null) {
            throw new NullPointerException("Plane must not be null!");
        }

        // Check if this plane has any other flights today, and if they
        // intersect with the proposed time. If they do, abort and return false.
        boolean intersect = dailyFlights.values().stream()
                .filter(x -> x.plane.equals(plane))
                .anyMatch(x -> x.estimatedDeparture.isBefore(estimatedArrival) &&
                                x.estimatedArrival.isAfter(estimatedDeparture));

        if (intersect) return null;

        // Get the next available flight number, construct the object and return true.
        int flightNumber = nextFlightNumber.getAndIncrement();

        Flight flight = getRuntime().getObjectsView().build()
                                .setType(Flight.class)
                                .setStreamName(getSMRObject().getStreamPrefix() + flightNumber)
                                .setArguments(plane, flightNumber, origin, estimatedDeparture,
                                        destination, estimatedArrival)
                                .open();

        dailyFlights.put(flightNumber, flight);
        return flight;
    }
}
