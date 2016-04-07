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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by mwei on 4/7/16.
 */
@SuppressWarnings("unchecked")
public class DailyFlightSchedules
{
    private Map<LocalDate, DailyFlightSchedule> dailySchedules;
    private CorfuRuntime runtime;
    private String databaseName;

    public DailyFlightSchedules(CorfuRuntime runtime, String databaseName) {
        this.runtime = runtime;
        this.databaseName = databaseName;
        dailySchedules = (Map<LocalDate, DailyFlightSchedule>)
                runtime.getObjectsView().build()
                            .setType(FGMap.class)
                            .setStreamName(databaseName + "schedules")
                            .open();
    }

    public DailyFlightSchedule getScheduleForDate(LocalDate date) {
        return runtime.getObjectsView().executeTX(() -> dailySchedules.computeIfAbsent(date,
                (k) ->  runtime.getObjectsView().build()
                    .setType(DailyFlightSchedule.class)
                    .setStreamName(databaseName + "schedule" + date.toString())
                    .setArguments(databaseName, date)
                    .open()));
    }

    public Flight addNewFlightToSchedule(Airplane plane, String origin, LocalDateTime departureTime,
                                         String destination, LocalDateTime arrivalTime) {
        DailyFlightSchedule schedule = getScheduleForDate(departureTime.toLocalDate());
        return schedule.addFlight(plane, origin, departureTime, destination, arrivalTime);
    }
}
