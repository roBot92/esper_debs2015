package onlab.esper_debs2015_projekt.main;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.espertech.esper.client.EPOnDemandPreparedQueryParameterized;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.time.CurrentTimeEvent;

import junit.framework.TestCase;
import onlab.event.TaxiLog;
import onlab.positioning.Cell;

/**
 * Unit test for simple App.
 */
public class Task2Test extends TestCase {

	private EPRuntime runtime;

	private static String driver1 = "license1";
	private static String driver2 = "license2";

	private static List<Cell> cells = (List<Cell>) Arrays.asList(new Cell(1, 1), new Cell(2, 2), new Cell(3, 3),
			new Cell(4, 4), new Cell(5, 5));

	@Before
	public void setUp() {
		EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();

		engine.getEPAdministrator().getConfiguration().addEventType(TaxiLog.class);

		// engine.getEPAdministrator().createEPL(App.getEplQuery(App.CONTEXT_DECLARATION));
		// EPStatement statement =
		// engine.getEPAdministrator().createEPL(App.getEplQuery(App.PREVIOUS_LOCATION_OF_TAXI));

		// statement.addListener(new Task2PreviousCellListener());

		//engine.getEPAdministrator().createEPL(App.getEplQuery(App.LOCATION_WINDOW_DECLARATION));
		/*EPStatement statement = engine.getEPAdministrator().createEPL(
				"insert into TaxiLogLocationWindow select hack_license, dropoff_cell, cast(true, boolean) as valid from TaxiLog");

		EPStatement statement2 = engine.getEPAdministrator().createEPL(
				"select count(*), dropoff_cell from TaxiLogLocationWindow where valid = true group by dropoff_cell");

		statement2.addListener((x, y) -> {
			for (EventBean event : x) {
				System.out.println(event.get("count(*)") + "\t" + event.get("dropoff_cell"));
			}
		});

		EPRuntime runtime = engine.getEPRuntime();
		// Set to external clock
		runtime.sendEvent(new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_EXTERNAL));

		this.runtime = runtime;*/
	}

	@Test
	public void test1() {
		long currentTime = 0;

		TaxiLog driver1Tlog1 = new TaxiLog();
		driver1Tlog1.setPickup_cell(cells.get(0));
		driver1Tlog1.setDropoff_cell(cells.get(1));
		driver1Tlog1.setHack_license(driver1);
		driver1Tlog1.setDropoff_datetime(new Date(currentTime));

		runtime.sendEvent(new CurrentTimeEvent(currentTime));
		runtime.sendEvent(driver1Tlog1);

		currentTime += 1000;
		TaxiLog driver2Tlog1 = new TaxiLog();
		driver2Tlog1.setPickup_cell(cells.get(2));
		driver2Tlog1.setDropoff_cell(cells.get(1));
		driver2Tlog1.setHack_license(driver2);
		driver2Tlog1.setDropoff_datetime(new Date(currentTime));
		runtime.sendEvent(new CurrentTimeEvent(currentTime));
		runtime.sendEvent(driver2Tlog1);

		currentTime += 1000;
		TaxiLog driver1Tlog2 = new TaxiLog();
		driver1Tlog2.setPickup_cell(cells.get(3));
		driver1Tlog2.setDropoff_cell(cells.get(2));
		driver1Tlog2.setHack_license(driver1);
		driver1Tlog2.setDropoff_datetime(new Date(currentTime));

		runtime.sendEvent(new CurrentTimeEvent(currentTime));
		runtime.sendEvent(driver1Tlog2);

		/*
		 * for (int i = 1; i < 60 * 60; i++) { currentTime += 1000;
		 * runtime.sendEvent(new CurrentTimeEvent(currentTime)); }
		 */

		EPOnDemandPreparedQueryParameterized updateQuery = runtime
				.prepareQueryWithParameters("update TaxiLogLocationWindow set valid=false where dropoff_cell = ?");

		updateQuery.setObject(1, cells.get(1));
		runtime.executeQuery(updateQuery);

		System.out.println("end");
	}

}
