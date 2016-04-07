package org.corfudb.example.airline.query;

import org.corfudb.example.airline.AirlineManager;
import org.corfudb.example.airline.objects.Aircraft;
import org.corfudb.example.airline.objects.Airplane;
import org.corfudb.example.airline.objects.DailyFlightSchedule;
import org.corfudb.runtime.view.AbstractViewTest;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by mwei on 4/7/16.
 */
public class DailyFlightSchedulesTest extends AbstractViewTest {

    AirlineManager am;

    @Before
    public void getSchedules() {
        am = new AirlineManager(getDefaultRuntime(), "testdb");
    }

    @Override
    public String getDefaultConfigurationString() {
        return getDefaultEndpoint();
    }

    @Test
    public void canAddAFlightToTheSchedule() {
        Airplane plane1 = am.airplanes.addPlane("ABC123", Aircraft.B787);
        am.schedules.addNewFlightToSchedule(plane1,
                "SFO", LocalDateTime.of(2016, 1, 1, 6, 0), "CDG", LocalDateTime.of(2016, 1, 1, 23, 0));
    }

    @Test
    public void cantUseSamePlaneOnTwoFlights() {
        Airplane plane1 = am.airplanes.addPlane("ABC123", Aircraft.B787);
        assertThat(am.schedules.addNewFlightToSchedule(plane1,
                "SFO", LocalDateTime.of(2016, 1, 1, 6, 0), "CDG", LocalDateTime.of(2016, 1, 1, 23, 0)))
                .isNotNull();
        assertThat(am.schedules.addNewFlightToSchedule(plane1,
                "SFO", LocalDateTime.of(2016, 1, 1, 6, 0), "CDG", LocalDateTime.of(2016, 1, 1, 23, 0)))
                .isEqualTo(null);
    }
}
