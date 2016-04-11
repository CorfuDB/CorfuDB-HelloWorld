package org.corfudb.example.airline.query;

import org.corfudb.example.airline.objects.Airplane;
import org.corfudb.example.airline.objects.DailyFlightSchedule;
import org.corfudb.example.airline.objects.Flight;
import org.corfudb.runtime.CorfuRuntime;
import org.corfudb.runtime.collections.FGMap;
import org.corfudb.runtime.collections.SMRMap;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** This class runs queries against the known flights in the system.
 * In our data model, flights are put into DailyFlightSchedules based on their departure time.
 * The same plane cannot be scheduled for use on two flights at the same time.
 * Created by mwei on 4/7/16.
 */
@SuppressWarnings("unchecked")
public class DailyFlightSchedules
{
    /** A map that holds daily flight schedules. This is really a cache, since it only holds references
     * to DailyFlightSchedule objects, which are computed dynamically by DailyFlightSchedule::getScheduleForDate.
     */
    private Map<LocalDate, DailyFlightSchedule> dailySchedules;
    /** The CorfuRuntime queries are executed against. */
    private CorfuRuntime runtime;
    /** The name of the database, which is used to prefix all stream IDs. */
    private String databaseName;

    /** Create a new daily flight schedule with the given runtime and database name.
     *
     * @param runtime       The runtime to use.
     * @param databaseName  The name of the database.
     */
    public DailyFlightSchedules(CorfuRuntime runtime, String databaseName) {
        this.runtime = runtime;
        this.databaseName = databaseName;
        dailySchedules = (Map<LocalDate, DailyFlightSchedule>)
                runtime.getObjectsView().build()
                            .setType(FGMap.class)
                            .setStreamName(databaseName + "schedules")
                            .open();
    }

    /** Get the flight schedule for a particular date.
     *
     * @param date          The date to get all flight schedules for.
     * @return              A flight schedule for a particular date.
     */
    public DailyFlightSchedule getScheduleForDate(LocalDate date) {
        return runtime.getObjectsView().executeTX(() -> dailySchedules.computeIfAbsent(date,
                (k) ->  runtime.getObjectsView().build()
                    .setType(DailyFlightSchedule.class)
                    .setStreamName(databaseName + "schedule" + date.toString())
                    .setArguments(databaseName, date)
                    .open()));
    }

    /** Get all flight schedules.
     *
     * @return              Get all flight schedules we know about.
     */
    public Collection<DailyFlightSchedule> getAllFlightSchedules() {
        return dailySchedules.values();
    }

    /** Add a new flight to the schedule, and return the flight. If the flight contains a plane which is
     * already scheduled to fly on another flight, fail and return null.
     * @param plane             The plane that is used for this flight.
     * @param origin            The origin, where the plane is to depart from.
     * @param departureTime     The departure time of the plane.
     * @param destination       The destination, where the plane is scheduled to arrive to.
     * @param arrivalTime       The scheduled arrival time for the plane.
     * @return                  A new flight if it was scheduled, or null, if there was
     *                          no flight scheduled.
     */
    public Flight addNewFlightToSchedule(Airplane plane, String origin, LocalDateTime departureTime,
                                         String destination, LocalDateTime arrivalTime) {
        DailyFlightSchedule schedule = getScheduleForDate(departureTime.toLocalDate());
        return schedule.addFlight(plane, origin, departureTime, destination, arrivalTime);
    }
}
