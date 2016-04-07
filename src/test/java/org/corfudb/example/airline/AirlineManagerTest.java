package org.corfudb.example.airline;

import org.corfudb.runtime.view.AbstractViewTest;
import org.junit.Test;

/**
 * Created by mwei on 4/6/16.
 */
public class AirlineManagerTest extends AbstractViewTest {

    @Override
    public String getDefaultConfigurationString() {
        return getDefaultEndpoint();
    }

    @Test
    public void canGetFlightByNumber()
    throws Exception
    {
        AirlineManager am = new AirlineManager(getDefaultRuntime(), "testdb");

    }
}
