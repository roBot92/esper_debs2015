package onlab.esper_debs2015_projekt.main;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import com.espertech.esper.client.EPOnDemandQueryResult;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.StatementAwareUpdateListener;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esper.client.time.TimerControlEvent;

import onlab.esper_deps2015_projekt.Task1Listener;
import onlab.event.Route;
import onlab.event.TaxiLog;
import onlab.main.DebsMain;
import onlab.positioning.CellHelper;
import onlab.utility.DataFileParser;
import onlab.utility.FrequentRoutesToplistSet;


public class App {
	private static String testQuery = "select count(*) as frequency, pickup_cell, dropoff_cell, max(dropoff_datetime) as last_inserted from TaxiLog#time(30 min)"
			+ "where pickup_cell is not null and dropoff_cell is not null group by pickup_cell, dropoff_cell";

	private static FrequentRoutesToplistSet<Route> freqRouteToplist = new FrequentRoutesToplistSet<Route>();

	public static void main(String[] args) throws Exception {

		List<TaxiLog> taxiLogs = null;
		// Initializing CellHelper for DataFileParser
		CellHelper chelper = new CellHelper(DebsMain.FIRST_CELL_X, DebsMain.FIRST_CELL_Y,
				DebsMain.SHIFT_X.divide(BigDecimal.valueOf(2)), DebsMain.SHIFT_Y.divide(BigDecimal.valueOf(2)), 600);

		EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();

		engine.getEPAdministrator().getConfiguration().addEventType(TaxiLog.class);
		EPStatement statement = engine.getEPAdministrator().createEPL(testQuery);
		
		EPRuntime runtime = engine.getEPRuntime();
		// Set to external clock
		runtime.sendEvent(new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_EXTERNAL));
		
		//Task1 toplist
		
		FrequentRoutesToplistSet<Route> routeToplist = new FrequentRoutesToplistSet<>();
		statement.addListener(new Task1Listener(routeToplist));

		
		try (DataFileParser dataFileParser = new DataFileParser(DebsMain.DATA_FILE_URL, DebsMain.DELIMITER,
				DebsMain.columncount, chelper)) {

			for (int i = 0; i < 1000; i++) {
				taxiLogs = dataFileParser.parseNextLinesFromCSVGroupedByDropoffDate();
				boolean currentTimeSent = false;
				for (TaxiLog tlog : taxiLogs) {
					if (!currentTimeSent) {
						runtime.sendEvent(new CurrentTimeEvent(tlog.getDropoff_datetime().getTime()));
						currentTimeSent = true;
					}
					runtime.sendEvent(tlog);
				}
				
				System.out.println(routeToplist);

			}
			
			
		
			
			
		} finally {
			engine.destroy();
		}

	}

}
