package hu.bme.mit.test.esper;


import java.util.Calendar;
import java.util.List;
import org.junit.Before;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.time.CurrentTimeEvent;

import hu.bme.mit.entities.TaxiLog;
import hu.bme.mit.esper.main.App;

public class EsperTask1Test extends hu.bme.mit.test.AbstractTask1Test{

	
	
	private EPRuntime runtime;


	@Before
	public void setUp() throws Exception {
		super.setUp();
		runtime = App.initializeEngineForTask1(toplist);
		rollPseudoClock(0);

	}


	@Override
	protected void insertTaxiLogs(List<TaxiLog> taxiLogs) {
		for(TaxiLog tlog:taxiLogs){
			runtime.sendEvent(tlog);
		}
		
	}


	@Override
	protected void rollPseudoClock(long time) {
		calendar.add(Calendar.MILLISECOND, (int)time);
		runtime.sendEvent(new CurrentTimeEvent(calendar.getTimeInMillis()));		
	}


	@Override
	protected void fireRules() {
		// NOOP		
	}

	

	

	
/*
	@Test
	public void testSlidingOut() {
		List<TaxiLog> tlogs = Arrays.asList(setUpTaxilog(cells.get(0), cells.get(1)),
				setUpTaxilog(cells.get(0), cells.get(1)), setUpTaxilog(cells.get(1), cells.get(1)),
				setUpTaxilog(cells.get(1), cells.get(1)), setUpTaxilog(cells.get(1), cells.get(2)),
				setUpTaxilog(cells.get(1), cells.get(2)), setUpTaxilog(cells.get(1), cells.get(3)),
				setUpTaxilog(cells.get(1), cells.get(3)), setUpTaxilog(cells.get(1), cells.get(4)),
				setUpTaxilog(cells.get(1), cells.get(4)), setUpTaxilog(cells.get(1), cells.get(5)),
				setUpTaxilog(cells.get(1), cells.get(5)), setUpTaxilog(cells.get(2), cells.get(1)),
				setUpTaxilog(cells.get(2), cells.get(1)), setUpTaxilog(cells.get(2), cells.get(2)),
				setUpTaxilog(cells.get(2), cells.get(2)), setUpTaxilog(cells.get(2), cells.get(3)),
				setUpTaxilog(cells.get(2), cells.get(3)), setUpTaxilog(cells.get(2), cells.get(4)),
				setUpTaxilog(cells.get(2), cells.get(4)), setUpTaxilog(cells.get(2), cells.get(5)));

		runtime.sendEvent(new CurrentTimeEvent(clock.getTimeInMillis()));
		for (int i = 0; i < tlogs.size() - 1; i++) {
			runtime.sendEvent(tlogs.get(i));
		}
		

		assertTrue("check1", toplist.size() == 10 && toplist.getSetSize() == 10);
		clock.add(Calendar.MINUTE, 15);
		
		runtime.sendEvent(new CurrentTimeEvent(clock.getTimeInMillis()));
		tlogs.get(20).setDropoff_datetime(new Date(clock.getTimeInMillis()));
		runtime.sendEvent(tlogs.get(20));

		Route route = new Route(cells.get(2), cells.get(5), tlogs.get(20).getDropoff_datetime(), -1);
		route.setFrequency(1);

		assertTrue("check2", toplist.size() == 10 && toplist.getSetSize() == 11 && !toplist.contains(route));
		
		clock.add(Calendar.MINUTE, 15);
		runtime.sendEvent(new CurrentTimeEvent(clock.getTimeInMillis()));
		assertTrue("check3", toplist.size() == 1 && toplist.getSetSize() == 1 && toplist.get(0).valueEquals(route));
	}
*/
	
}
