package org.corfudb.example.airline.objects;

import org.corfudb.runtime.object.*;
import org.corfudb.runtime.view.ObjectOpenOptions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by mwei on 4/7/16.
 */
@CorfuObject(objectType= ObjectType.SMR,
        constructorType= ConstructorType.PERSISTED,
        stateType = DailyFlightSchedule.class,
        stateSource = StateSource.SELF
)
public class DailyFlightSchedule implements ICorfuSMRObject<DailyFlightSchedule> {

    Map<Integer, Flight> dailyFlights = new ConcurrentHashMap<>();
    AtomicInteger nextFlightNumber = new AtomicInteger(1);
    String databaseName;
    LocalDate dateTime;

    public DailyFlightSchedule(String databaseName, LocalDate dateTime) {
        this.databaseName = databaseName;
        this.dateTime = dateTime;
    }

    @DontInstrument
    public String getStreamPrefix() {
        return databaseName + dateTime.toString();
    }

    @MutatorAccessor
    public Flight addFlight(Airplane plane, String origin, LocalDateTime estimatedDeparture,
                             String destination, LocalDateTime estimatedArrival)
    {
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
                                .addOption(ObjectOpenOptions.CREATE_ONLY)
                                .open();

        dailyFlights.put(flightNumber, flight);
        return flight;
    }
}
