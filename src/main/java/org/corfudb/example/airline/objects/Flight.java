package org.corfudb.example.airline.objects;

import org.corfudb.runtime.object.ConstructorType;
import org.corfudb.runtime.object.CorfuObject;
import org.corfudb.runtime.object.ObjectType;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * Created by mwei on 4/6/16.
 */
@CorfuObject(objectType= ObjectType.STATELESS,
        constructorType= ConstructorType.PERSISTED)
public class Flight {

    final Airplane plane;
    final int flightNumber;
    final String origin;
    final String destination;

    final LocalDateTime estimatedDeparture;
    final LocalDateTime estimatedArrival;

    public Flight(Airplane plane, int flightNumber,
                  String origin, LocalDateTime estimatedDeparture, String destination,
                   LocalDateTime estimatedArrival) {
        this.plane = plane;
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.estimatedDeparture = estimatedDeparture;
        this.estimatedArrival = estimatedArrival;
    }
}
