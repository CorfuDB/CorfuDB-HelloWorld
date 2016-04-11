package org.corfudb.example.airline.objects;

import org.corfudb.runtime.object.ConstructorType;
import org.corfudb.runtime.object.CorfuObject;
import org.corfudb.runtime.object.ObjectType;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * This object encapsulates data for a flight.
 * Currently, it is a stateless object, which means that all the data is contained
 * is stored at creation time and no further modifications are made (as if the object
 * was effectively final).
 * Created by mwei on 4/6/16.
 */
@CorfuObject(objectType= ObjectType.STATELESS,
        constructorType= ConstructorType.PERSISTED)
public class Flight {

    /** The plane used for this flight. */
    final Airplane plane;
    /** The flight number for this flight. */
    final int flightNumber;
    /** The origin of this flight. */
    final String origin;
    /** The destination of this flight. */
    final String destination;

    /** When the flight is estimated to depart. */
    final LocalDateTime estimatedDeparture;
    /** When the flight is estimated to arrive. */
    final LocalDateTime estimatedArrival;

    /** Create a new Flight object.
     *
     * @param plane                 The plane to use.
     * @param flightNumber          The number of the flight.
     * @param origin                Where the plane comes from.
     * @param estimatedDeparture    When the plane is estimated to depart.
     * @param destination           Where the plane will land.
     * @param estimatedArrival      When the plane is estimated to arrive.
     */
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

    /**
     * Returns a string representation of the object. In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p>
     * The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return String.format("FL%2d %s-%s dep %s arr %s [%s]", flightNumber, origin,
                destination, estimatedDeparture, estimatedArrival, plane.toString());
    }
}
