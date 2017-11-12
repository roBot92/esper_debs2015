package onlab.esper_debs2015_projekt.main;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.time.CurrentTimeEvent;

import onlab.positioning.Cell;
import onlab.event.Route;
import onlab.event.TaxiLog;
import onlab.utility.FrequentRoutesToplistSet;

public class EsperTask1Test {

	private FrequentRoutesToplistSet toplist;
	private static List<Cell> cells;
	private static List<TaxiLog> route1tlogs;
	private static List<TaxiLog> route2tlogs;
	private static List<TaxiLog> route3tlogs;
	private Calendar clock;
	private EPRuntime runtime;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// calendar = Calendar.getInstance();
		cells = Arrays.asList(new Cell(1, 1), new Cell(1, 2), new Cell(2, 1), new Cell(2, 2), new Cell(3, 1),
				new Cell(3, 2));

		route1tlogs = Arrays.asList(setUpTaxilog(cells.get(0), cells.get(1)/* , getZeroTimeCalendar() */),
				setUpTaxilog(cells.get(0), cells.get(1)/* , getZeroTimeCalendar() */),
				setUpTaxilog(cells.get(0), cells.get(1)/* , getZeroTimeCalendar() */),
				setUpTaxilog(cells.get(0), cells.get(1)/* , getZeroTimeCalendar() */));

		route2tlogs = Arrays.asList(setUpTaxilog(cells.get(2), cells.get(3)/* , getZeroTimeCalendar() */),
				setUpTaxilog(cells.get(2), cells.get(3)/* , getZeroTimeCalendar() */),
				setUpTaxilog(cells.get(2), cells.get(3)/* , getZeroTimeCalendar() */),
				setUpTaxilog(cells.get(2), cells.get(3)/* , getZeroTimeCalendar() */));

		route3tlogs = Arrays.asList(setUpTaxilog(cells.get(4), cells.get(5)/* , getZeroTimeCalendar() */),
				setUpTaxilog(cells.get(4), cells.get(5)/* , getZeroTimeCalendar() */),
				setUpTaxilog(cells.get(4), cells.get(5)/* , getZeroTimeCalendar() */),
				setUpTaxilog(cells.get(4), cells.get(5)/* , getZeroTimeCalendar() */));

	}

	@Before
	public void setUp() throws Exception {
		toplist = new FrequentRoutesToplistSet();
		runtime = App.initializeEngineForTask1(toplist);
		clock = Calendar.getInstance();
		clock.setTimeInMillis(0);

		for (int i = 0; i < 4; i++) {
			route1tlogs.get(i).setDropoff_datetime(getZeroTimeCalendar());
			route2tlogs.get(i).setDropoff_datetime(getZeroTimeCalendar());
			route3tlogs.get(i).setDropoff_datetime(getZeroTimeCalendar());
		}

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test_insertOneTaxiLog() {
		TaxiLog tlog1 = route1tlogs.get(0);
		Route route = new Route(tlog1.getPickup_cell(), tlog1.getDropoff_cell(), tlog1.getDropoff_datetime(), 1);

		runtime.sendEvent(new CurrentTimeEvent(clock.getTimeInMillis()));
		runtime.sendEvent(tlog1);

		boolean check = route.valueEquals(toplist.get(0)) && toplist.size() == 1;
		assertTrue(check);
	}

	@Test
	public void test_sortByFrequency() {
		TaxiLog tlog1 = route1tlogs.get(0);
		TaxiLog tlog2 = route2tlogs.get(0);
		TaxiLog tlog3 = route1tlogs.get(1);

		Route route1 = new Route(tlog1.getPickup_cell(), tlog1.getDropoff_cell(), tlog1.getDropoff_datetime(), 1);
		Route route2 = new Route(tlog2.getPickup_cell(), tlog2.getDropoff_cell(), tlog2.getDropoff_datetime(), 1);
		Route route3 = new Route(tlog3.getPickup_cell(), tlog3.getDropoff_cell(), tlog3.getDropoff_datetime(), 2);
		runtime.sendEvent(new CurrentTimeEvent(clock.getTimeInMillis()));
		runtime.sendEvent(tlog1);
		runtime.sendEvent(tlog2);

		boolean check = toplist.size() == 2 && route2.valueEquals(toplist.get(0)) && route1.valueEquals(toplist.get(1));
		assertTrue("check1", check);

		runtime.sendEvent(tlog3);
		check = toplist.size() == 2 && route2.valueEquals(toplist.get(1)) && route3.valueEquals(toplist.get(0));
		assertTrue("check1", check);

	}

	@Test
	public void testAgeing() {

		// First minute +1 route1, +1 route2, +1 route3
		runtime.sendEvent(new CurrentTimeEvent(clock.getTimeInMillis()));
		runtime.sendEvent(route1tlogs.get(0));
		runtime.sendEvent(route2tlogs.get(0));
		runtime.sendEvent(route3tlogs.get(0));

		Route route1 = new Route(route1tlogs.get(0).getPickup_cell(), route1tlogs.get(0).getDropoff_cell(),
				route1tlogs.get(0).getDropoff_datetime(), 1);
		Route route2 = new Route(route2tlogs.get(0).getPickup_cell(), route2tlogs.get(0).getDropoff_cell(),
				route2tlogs.get(0).getDropoff_datetime(), 1);
		Route route3 = new Route(route3tlogs.get(0).getPickup_cell(), route3tlogs.get(0).getDropoff_cell(),
				route3tlogs.get(0).getDropoff_datetime(), 1);
		assertTrue("check1", toplist.size() == 3 && toplist.get(0).valueEquals(route3)
				&& toplist.get(1).valueEquals(route2) && toplist.get(2).valueEquals(route1));

		// Second minute, +1 route1, +1 route2
		clock.add(Calendar.MINUTE, 1);

		runtime.sendEvent(new CurrentTimeEvent(clock.getTimeInMillis()));
		route1tlogs.get(1).setDropoff_datetime(new Date(clock.getTimeInMillis()));
		route2tlogs.get(1).setDropoff_datetime(new Date(clock.getTimeInMillis()));
		runtime.sendEvent(route1tlogs.get(1));
		runtime.sendEvent(route2tlogs.get(1));
		route1.setFrequency(2);
		route1.setLastDropoffTime(clock.getTime());
		route2.setFrequency(2);
		route2.setLastDropoffTime(clock.getTime());

		assertTrue("check2", toplist.size() == 3 && toplist.get(0).valueEquals(route2)
				&& toplist.get(1).valueEquals(route1) && toplist.get(2).valueEquals(route3));

		// Third minute +1 route1
		clock.add(Calendar.MINUTE, 1);
		runtime.sendEvent(new CurrentTimeEvent(clock.getTimeInMillis()));
		route1tlogs.get(2).setDropoff_datetime(new Date(clock.getTimeInMillis()));
		route1.setFrequency(3);
		route1.setLastDropoffTime(clock.getTime());
		runtime.sendEvent(route1tlogs.get(2));
		assertTrue("check3", toplist.size() == 3 && toplist.get(0).valueEquals(route1)
				&& toplist.get(1).valueEquals(route2) && toplist.get(2).valueEquals(route3));

		clock.add(Calendar.MINUTE, 28);
		runtime.sendEvent(new CurrentTimeEvent(clock.getTimeInMillis()));
		route1.setFrequency(2);
		route2.setFrequency(1);
		assertTrue("check4",
				toplist.size() == 2 && toplist.get(0).valueEquals(route1) && toplist.get(1).valueEquals(route2));

		clock.add(Calendar.MINUTE, 1);
		runtime.sendEvent(new CurrentTimeEvent(clock.getTimeInMillis()));
		route1.setFrequency(1);

		assertTrue("check5", toplist.size() == 1 && toplist.get(0).valueEquals(route1));

		clock.add(Calendar.MINUTE, 1);
		runtime.sendEvent(new CurrentTimeEvent(clock.getTimeInMillis()));

		assertTrue("check6", toplist.size() == 0);

	}

	@Test
	public void test_slidingOut() {
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

	private Date getZeroTimeCalendar() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(0);
		return cal.getTime();
	}

	private static TaxiLog setUpTaxilog(Cell startCell, Cell endCell) {
		TaxiLog tlog = new TaxiLog();
		Calendar zeroCalendar = Calendar.getInstance();
		tlog.setPickup_cell(startCell);
		tlog.setDropoff_cell(endCell);

		zeroCalendar.setTimeInMillis(0);
		tlog.setDropoff_datetime(zeroCalendar.getTime());
		return tlog;
	}

}
