package onlab.esper_debs2015_projekt.main;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.After;
//import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.espertech.esper.client.EPOnDemandPreparedQueryParameterized;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.time.CurrentTimeEvent;

import onlab.event.AreaWithProfit;
import onlab.event.TaxiLog;
import onlab.positioning.Cell;
import onlab.utility.ProfitableAreaToplistSet;

public class EsperTask2Test {

	private ProfitableAreaToplistSet toplist;
	private static List<Cell> cells;

	private Calendar clock;
	private EPRuntime runtime;
	private EPOnDemandPreparedQueryParameterized updateNamedWindowQuery;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		cells = Arrays.asList(new Cell(1, 1), new Cell(1, 2), new Cell(1, 3), new Cell(2, 1), new Cell(2, 2),
				new Cell(2, 3), new Cell(3, 1), new Cell(3, 2), new Cell(3, 3), new Cell(4, 1), new Cell(4, 2));

	}

	@Before
	public void setUp() throws Exception {
		toplist = new ProfitableAreaToplistSet();
		runtime = App.initializeEngineForTask2(toplist);
		updateNamedWindowQuery = runtime
				.prepareQueryWithParameters(App.getEplQuery(App.ONDEMAND_UPDATE_NAMED_WINDOW_QUERY));
		clock = Calendar.getInstance();
		clock.setTimeInMillis(0);
	}

	@After
	public void tearDown() {
		EPServiceProviderManager.getDefaultProvider().destroy();
	}

	@Test
	public void insertForOneArea_Test() {
		TaxiLog tlog1 = setUpTaxilog(cells.get(0), cells.get(1), BigDecimal.ONE, BigDecimal.ONE, "1");
		TaxiLog tlog2 = setUpTaxilog(cells.get(0), cells.get(1), BigDecimal.ONE, BigDecimal.TEN, "2");

		AreaWithProfit area = new AreaWithProfit(cells.get(0), BigDecimal.valueOf(2),
				new Date(clock.getTimeInMillis()));

		sendEvents(Arrays.asList(tlog1), true);

		assertTrue("check1", area.valueEquals(toplist.get(0)) && toplist.size() == 1);

		clock.add(Calendar.MINUTE, 1);
		tlog2.setDropoff_datetime(new Date(clock.getTimeInMillis()));
		area.setLastInserted(new Date(clock.getTimeInMillis()));
		area.setMedianProfitIndex(BigDecimal.valueOf(6.5));

		sendEvents(Arrays.asList(tlog2), true);

		assertTrue("check2", area.valueEquals(toplist.get(0)) && toplist.size() == 1);

	}

	@Test
	public void insertForTwoArea_Test() {
		List<TaxiLog> tlogs = Arrays.asList(
				setUpTaxilog(cells.get(0), cells.get(1), BigDecimal.ONE, BigDecimal.ONE, "1"),
				setUpTaxilog(cells.get(0), cells.get(1), BigDecimal.ONE, BigDecimal.TEN, "2"),
				setUpTaxilog(cells.get(0), cells.get(1), BigDecimal.TEN, BigDecimal.TEN, "3"),
				setUpTaxilog(cells.get(1), cells.get(0), BigDecimal.ONE, BigDecimal.ONE, "4"),
				setUpTaxilog(cells.get(1), cells.get(0), BigDecimal.ONE, BigDecimal.ONE, "5"),
				setUpTaxilog(cells.get(1), cells.get(0), BigDecimal.TEN, BigDecimal.TEN, "6"));
		BigDecimal emptyTaxes = BigDecimal.valueOf(3);
		BigDecimal medianProfit1 = BigDecimal.valueOf(11).divide(emptyTaxes, 2, BigDecimal.ROUND_HALF_UP);
		BigDecimal medianProfit2 = BigDecimal.valueOf(2).divide(emptyTaxes, 2, BigDecimal.ROUND_HALF_UP);
		AreaWithProfit area1 = new AreaWithProfit(cells.get(0), medianProfit1, new Date(clock.getTimeInMillis()));
		AreaWithProfit area2 = new AreaWithProfit(cells.get(1), medianProfit2, new Date(clock.getTimeInMillis()));

		sendEvents(tlogs, true);

		assertTrue(toplist.size() == 2 && area1.valueEquals(toplist.get(0)) && area2.valueEquals(toplist.get(1)));

	}

	@Test
	public void lessEmptyTaxiByNewDrive_Test() {
		List<TaxiLog> tlogs = Arrays.asList(
				setUpTaxilog(cells.get(0), cells.get(1), BigDecimal.ONE, BigDecimal.ONE, "1"),
				setUpTaxilog(cells.get(0), cells.get(1), BigDecimal.ONE, BigDecimal.TEN, "2"),
				setUpTaxilog(cells.get(1), cells.get(0), BigDecimal.ONE, BigDecimal.ONE, "3"),
				setUpTaxilog(cells.get(1), cells.get(0), BigDecimal.ONE, BigDecimal.ONE, "4"),
				setUpTaxilog(cells.get(0), cells.get(2), BigDecimal.TEN, BigDecimal.TEN, "3"));
		AreaWithProfit area1 = new AreaWithProfit(cells.get(0), BigDecimal.valueOf(3.25),
				new Date(clock.getTimeInMillis()));
		AreaWithProfit area2 = new AreaWithProfit(cells.get(1), BigDecimal.valueOf(1),
				new Date(clock.getTimeInMillis()));

		sendEvents(Arrays.asList(tlogs.get(0), tlogs.get(1), tlogs.get(2), tlogs.get(3)), true);

		assertTrue("firstCheck",
				toplist.size() == 2 && area1.valueEquals(toplist.get(0)) && area2.valueEquals(toplist.get(1)));

		clock.add(Calendar.MINUTE, 10);
		tlogs.get(4).setDropoff_datetime(new Date(clock.getTimeInMillis()));
		sendEvents(Arrays.asList(tlogs.get(4)), true);

		area1.setLastInserted(new Date(clock.getTimeInMillis()));
		area1.setMedianProfitIndex(BigDecimal.valueOf(11));
		assertTrue("secondCheck",
				toplist.size() == 2 && area1.valueEquals(toplist.get(0)) && area2.valueEquals(toplist.get(1)));
	}

	@Test
	public void medianChangingAfter15Minutes_Test() {
		TaxiLog tlog1 = setUpTaxilog(cells.get(0), cells.get(1), BigDecimal.ONE, BigDecimal.ONE, "1");

		sendEvents(Arrays.asList(tlog1), true);
		TaxiLog tlog2 = setUpTaxilog(cells.get(0), cells.get(1), BigDecimal.ONE, BigDecimal.TEN, "2");

		clock.add(Calendar.MINUTE, 1);
		tlog2.setPickup_datetime(new Date(clock.getTimeInMillis()));
		clock.add(Calendar.MINUTE, 1);
		tlog2.setDropoff_datetime(new Date(clock.getTimeInMillis()));
		sendEvents(Arrays.asList(tlog2), true);

		AreaWithProfit area = new AreaWithProfit(cells.get(0), BigDecimal.valueOf(6.5),
				new Date(clock.getTimeInMillis()));

		assertTrue("check1", area.valueEquals(toplist.get(0)) && toplist.size() == 1);

		clock.add(Calendar.MINUTE, 14);
		sendEvents(Collections.emptyList(), true);

		area.setMedianProfitIndex(BigDecimal.valueOf(11));

		assertTrue("check2", area.valueEquals(toplist.get(0)) && toplist.size() == 1);

		clock.add(Calendar.SECOND, 61);
		sendEvents(Collections.emptyList(), true);

		assertTrue("check3", toplist.isEmpty());
	}
	/*
	 * @Test public void overFlowingAndAging_test() { List<TaxiLog> tlogs =
	 * Arrays.asList( setUpTaxilog(cells.get(0), cells.get(10), BigDecimal.TEN,
	 * BigDecimal.ONE, "1"), setUpTaxilog(cells.get(1), cells.get(9),
	 * BigDecimal.ONE, BigDecimal.TEN, "2"), setUpTaxilog(cells.get(2),
	 * cells.get(8), BigDecimal.TEN, BigDecimal.TEN, "3"),
	 * setUpTaxilog(cells.get(3), cells.get(7), BigDecimal.ONE, BigDecimal.TEN,
	 * "4"), setUpTaxilog(cells.get(4), cells.get(6), BigDecimal.TEN,
	 * BigDecimal.ONE, "5"), setUpTaxilog(cells.get(5), cells.get(5),
	 * BigDecimal.TEN, BigDecimal.TEN, "6"), setUpTaxilog(cells.get(6),
	 * cells.get(4), BigDecimal.TEN, BigDecimal.TEN, "7"),
	 * setUpTaxilog(cells.get(7), cells.get(3), BigDecimal.TEN, BigDecimal.TEN,
	 * "8"), setUpTaxilog(cells.get(8), cells.get(2), BigDecimal.TEN,
	 * BigDecimal.TEN, "9"), setUpTaxilog(cells.get(9), cells.get(0),
	 * BigDecimal.TEN, BigDecimal.TEN, "10"), setUpTaxilog(cells.get(10),
	 * cells.get(0), BigDecimal.ONE, BigDecimal.ONE, "11"));
	 * 
	 * clock.add(Calendar.SECOND, 1);
	 * 
	 * for (int i = 0; i < 11; i++) { tlogs.get(i).setDropoff_datetime(new
	 * Date(clock.getTimeInMillis()));
	 * tlogs.get(i).setInserted(System.currentTimeMillis());
	 * medProcPushable.push(Arrays.asList(tlogs.get(i))); clock.add(Calendar.MINUTE,
	 * 1); medProcPushable.push(new Tick(clock.getTimeInMillis()));
	 * 
	 * }
	 * 
	 * clock.advanceTime(1, TimeUnit.MINUTES); kSession.insert(new
	 * Tick(clock.getTimeInMillis())); medProcPushable.push(new
	 * Tick(clock.getTimeInMillis()));
	 * 
	 * clock.advanceTime(4, TimeUnit.MINUTES); kSession.insert(new
	 * Tick(clock.getTimeInMillis(), System.currentTimeMillis()));
	 * medProcPushable.push(new Tick(clock.getTimeInMillis()));
	 * 
	 * System.out.println(toplist);
	 * 
	 * for (int i = 0; i < 15; i++) { clock.advanceTime(1, TimeUnit.MINUTES);
	 * kSession.insert(new Tick(clock.getTimeInMillis(),
	 * System.currentTimeMillis())); medProcPushable.push(new
	 * Tick(clock.getTimeInMillis())); System.out.println(toplist); }
	 * 
	 * QueryResults qres = kSession.getQueryResults("taxis"); TaxiLog tlog =
	 * setUpTaxilog(cells.get(0), cells.get(10), BigDecimal.ONE, BigDecimal.ONE,
	 * "12"); tlog.setPickup_datetime(new Date(clock.getTimeInMillis()));
	 * clock.advanceTime(1, TimeUnit.MINUTES); tlog.setDropoff_datetime(new
	 * Date(clock.getTimeInMillis()));
	 * 
	 * kSession.insert(new Tick(clock.getTimeInMillis())); kSession.insert(tlog);
	 * medProcPushable.push(new Tick(clock.getTimeInMillis()));
	 * 
	 * System.out.println(toplist);
	 * 
	 * clock.advanceTime(12, TimeUnit.MINUTES); // debug
	 * 
	 * kSession.insert(new Tick(clock.getTimeInMillis())); medProcPushable.push(new
	 * Tick(clock.getTimeInMillis())); QueryResults qres =
	 * kSession.getQueryResults("areas"); System.out.println(toplist);
	 * 
	 * }
	 */

	private static TaxiLog setUpTaxilog(Cell startCell, Cell endCell, BigDecimal fare, BigDecimal tip,
			String hack_License) {
		TaxiLog tlog = new TaxiLog();
		Calendar zeroCalendar = Calendar.getInstance();
		tlog.setPickup_cell(startCell);
		tlog.setDropoff_cell(endCell);

		zeroCalendar.setTimeInMillis(0);
		tlog.setDropoff_datetime(zeroCalendar.getTime());
		tlog.setPickup_datetime(zeroCalendar.getTime());

		tlog.setFare_amount(fare);
		tlog.setTip_amount(tip);

		tlog.setHack_license(hack_License);
		return tlog;
	}

	private void sendEvents(List<TaxiLog> taxiLogs, boolean sendTime) {
		if (sendTime) {
			runtime.sendEvent(new CurrentTimeEvent(clock.getTimeInMillis()));
		}
		for (TaxiLog tlog : taxiLogs) {
			updateNamedWindowQuery.setObject(1, tlog.getHack_license());
			runtime.executeQuery(updateNamedWindowQuery);
			runtime.sendEvent(tlog);
		}
	}
}
