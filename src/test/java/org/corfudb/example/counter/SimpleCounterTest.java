package org.corfudb.example.counter;

import org.corfudb.example.org.corfudb.example.counter.SimpleCounterExample;
import org.corfudb.runtime.view.AbstractViewTest;
import org.junit.Test;

/**
 * Created by dalia on 6/28/16.
 */
public class SimpleCounterTest extends AbstractViewTest {
    @Override
    public String getDefaultConfigurationString() {
        return getDefaultEndpoint();
    }

    @Test
    public void canOpenSharedCounter()
            throws Exception
    {
        SimpleCounterExample simpleCounter =  new SimpleCounterExample(getDefaultRuntime());
        simpleCounter.getSharedCounter();

    }

    @Test
    public void canSetSharedCounter()
            throws Exception
    {
        SimpleCounterExample simpleCounter =  new SimpleCounterExample(getDefaultRuntime());
        SimpleCounterExample.SharedCounter cnt = simpleCounter.getSharedCounter();
        cnt.setCnt(44);

    }
    @Test
    public void canGetSharedCounter()
            throws Exception
    {
        SimpleCounterExample simpleCounter =  new SimpleCounterExample(getDefaultRuntime());
        SimpleCounterExample.SharedCounter cnt = simpleCounter.getSharedCounter();
        cnt.getCnt();
    }
    @Test
    public void canSetAndGetSharedCounter()
            throws Exception
    {
        SimpleCounterExample simpleCounter =  new SimpleCounterExample(getDefaultRuntime());
        SimpleCounterExample.SharedCounter cnt = simpleCounter.getSharedCounter();
        cnt.setCnt(44);
        assert cnt.getCnt() == 44;
    }
}
