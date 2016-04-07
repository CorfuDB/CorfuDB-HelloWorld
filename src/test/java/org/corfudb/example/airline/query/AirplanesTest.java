package org.corfudb.example.airline.query;

import org.corfudb.example.airline.AirlineManager;
import org.corfudb.example.airline.objects.Aircraft;
import org.corfudb.example.airline.objects.Airplane;
import org.corfudb.runtime.exceptions.ObjectExistsException;
import org.corfudb.runtime.view.AbstractViewTest;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by mwei on 4/6/16.
 */
public class AirplanesTest extends AbstractViewTest {

    Airplanes airplanes;

    @Before
    public void getAirplanes() {
        AirlineManager am = new AirlineManager(getDefaultRuntime(), "testdb");
        this.airplanes = am.airplanes;
    }

    @Override
    public String getDefaultConfigurationString() {
        return getDefaultEndpoint();
    }

    @Test
    public void canAddAnAirplane()
            throws Exception
    {
        Airplane ap = airplanes.addPlane("ABC123", Aircraft.B787);
        assertThat(ap.aircraft)
                .isEqualTo(Aircraft.B787);
        assertThat(ap.tailNumber)
                .isEqualTo("ABC123");
    }

    @Test
    public void cannotAddTwoOfTheSamePlanes() {
        Airplane ap = airplanes.addPlane("ABC123", Aircraft.B787);
        assertThatThrownBy(() -> airplanes.addPlane("ABC123", Aircraft.B787))
                .isInstanceOf(ObjectExistsException.class);
    }

    @Test
    public void canAddAndGetAnAirplane()
            throws Exception
    {
        Airplane ap = airplanes.addPlane("ABC123", Aircraft.B787);

        Airplane a2 = airplanes.getPlaneByTailNumber("ABC123");
        assertThat(a2.tailNumber)
                .isEqualTo("ABC123");
        assertThat(a2.aircraft)
                .isEqualTo(Aircraft.B787);
    }

    @Test
    public void airplineNotInMapReturnsNull()
            throws Exception
    {
        Airplane ap = airplanes.addPlane("ABC123", Aircraft.B787);

        Airplane a2 = airplanes.getPlaneByTailNumber("ABC124");
        assertThat(a2)
                .isEqualTo(null);
    }
}
