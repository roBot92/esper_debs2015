package onlab.esper_debs2015_projekt.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import com.espertech.esper.client.EPOnDemandPreparedQueryParameterized;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esper.client.time.TimerControlEvent;

import onlab.esper_deps2015_projekt.listeners.Task1Listener;
import onlab.esper_deps2015_projekt.listeners.Task2CountOfEmptyTaxesListener;
import onlab.esper_deps2015_projekt.listeners.Task2MedianListener;
import onlab.event.TaxiLog;
import onlab.main.DebsMain;
import onlab.positioning.Cell;
import onlab.positioning.CellHelper;
import onlab.utility.DataFileParser;
import onlab.utility.FrequentRoutesToplistSet;
import onlab.utility.ProfitableAreaToplistSet;

public class App {
	static String TASK1_QUERY = "C:\\Users\\Boti\\git_exper\\esper_debs2015_projekt\\src\\main\\resources\\onlab\\esper_debs2015_projekt\\Task1Query.sql";

	static String MEDIAN_OF_CELL = "C:\\Users\\Boti\\git_exper\\esper_debs2015_projekt\\src\\main\\resources\\onlab\\esper_debs2015_projekt\\Task2GetMedianByCell.sql";
	static String NAMED_WINDOW_DECLARATION = "C:\\Users\\Boti\\git_exper\\esper_debs2015_projekt\\src\\main\\resources\\onlab\\esper_debs2015_projekt\\Task2LocationNamedWindowDeclaration.sql";
	static String COUNT_OF_EMPTY_TAXILOGS_BY_CELL = "C:\\Users\\Boti\\git_exper\\esper_debs2015_projekt\\src\\main\\resources\\onlab\\esper_debs2015_projekt\\Task2CountOfValidTaxiLogLocations.sql";
	static String NAMED_WINDOW_INSERTION_QUERY = "C:\\Users\\Boti\\git_exper\\esper_debs2015_projekt\\src\\main\\resources\\onlab\\esper_debs2015_projekt\\Task2InsertIntoLocationNamedWindow.sql";
	static String ONDEMAND_UPDATE_NAMED_WINDOW_QUERY = "C:\\Users\\Boti\\git_exper\\esper_debs2015_projekt\\src\\main\\resources\\onlab\\esper_debs2015_projekt\\Task2OnDemandUpdateParametrizedQuery.sql";

	static FrequentRoutesToplistSet freqRouteToplist = new FrequentRoutesToplistSet();
	static ProfitableAreaToplistSet mostProfArea = new ProfitableAreaToplistSet();

	private static long TEST_INTERVAL_IN_IN_MS = 1 * 60 * 60 * 1000;

	public static void main(String[] args) {

		try {
			long currentTime = System.currentTimeMillis();
			//runTask1();
			long elapsed = (System.currentTimeMillis() - currentTime) / 1000;
			System.out.println("Task1: " + elapsed + " seconds.");

			currentTime = System.currentTimeMillis();
			runTask2();
			elapsed = (System.currentTimeMillis() - currentTime) / 1000;
			System.out.println("Task2: " + elapsed + " seconds.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	public static EPRuntime initializeEngineForTask1(FrequentRoutesToplistSet toplist) {
		EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();

		engine.getEPAdministrator().getConfiguration().addEventType(TaxiLog.class);
		EPStatement statement = engine.getEPAdministrator().createEPL(getEplQuery(TASK1_QUERY));

		EPRuntime runtime = engine.getEPRuntime();
		// Set to external clock
		runtime.sendEvent(new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_EXTERNAL));
		statement.addListener(new Task1Listener(toplist));
		return runtime;
	}

	public static EPRuntime initializeEngineForTask2(ProfitableAreaToplistSet toplist) {

		EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();

		
		engine.getEPAdministrator().getConfiguration().addEventType(TaxiLog.class);

		engine.getEPAdministrator().createEPL(getEplQuery(NAMED_WINDOW_DECLARATION));
		engine.getEPAdministrator().createEPL(getEplQuery(NAMED_WINDOW_INSERTION_QUERY));
		engine.getEPAdministrator().createEPL("create index hlIndex on TaxiLogLocationWindow(hack_license)");
		
		
		EPStatement medianStatement = engine.getEPAdministrator().createEPL(getEplQuery(MEDIAN_OF_CELL));
		medianStatement.addListener(new Task2MedianListener(toplist));
		EPStatement countOfEmptyTaxesStatement = engine.getEPAdministrator()
				.createEPL(getEplQuery(COUNT_OF_EMPTY_TAXILOGS_BY_CELL));
		countOfEmptyTaxesStatement.addListener(new Task2CountOfEmptyTaxesListener(toplist));

		
		EPRuntime runtime = engine.getEPRuntime();
		
		// Set to external clock
		runtime.sendEvent(new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_EXTERNAL));

		return runtime;
	}

	public static String getEplQuery(String fileName) {
		Scanner scanner = null;
		StringBuilder result = new StringBuilder();

		try {
			scanner = new Scanner(new File(fileName));

			while (scanner.hasNextLine()) {
				result.append(scanner.nextLine());
				result.append("\n");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}

		return result.toString();

	}

	public static void runTask1() throws FileNotFoundException {
		CellHelper chelper = new CellHelper(DebsMain.FIRST_CELL_X, DebsMain.FIRST_CELL_Y,
				DebsMain.SHIFT_X.divide(BigDecimal.valueOf(2)), DebsMain.SHIFT_Y.divide(BigDecimal.valueOf(2)), 600);
		List<TaxiLog> taxiLogs = null;

		EPRuntime runtime = initializeEngineForTask1(freqRouteToplist);

		try (DataFileParser dataFileParser = new DataFileParser(DebsMain.DATA_FILE_URL, DebsMain.DELIMITER,
				DebsMain.columncount, chelper)) {

			taxiLogs = dataFileParser.parseNextLinesFromCSVGroupedByDropoffDate();
			long currentTime = DataFileParser.getCURRENT_TIME();
			long startingTime = DataFileParser.getCURRENT_TIME();

			long countOfProcessedTlogs = 0;
			while (currentTime - startingTime <= TEST_INTERVAL_IN_IN_MS) {
				runtime.sendEvent(new CurrentTimeEvent(currentTime));
				if (currentTime >= DataFileParser.getCURRENT_TIME()) {
					for (TaxiLog tlog : taxiLogs) {
						tlog.setInserted(System.currentTimeMillis());
						runtime.sendEvent(tlog);
						countOfProcessedTlogs++;
					}
					taxiLogs = dataFileParser.parseNextLinesFromCSVGroupedByDropoffDate();
					// System.out.println(freqRouteToplist);
				}

				currentTime += 1000;
				if ((currentTime - startingTime) % (1000 * 60) == 0) {
					System.out.println(freqRouteToplist);
				}
			}
			
			System.out.println("Processed tlog in Task1: " + countOfProcessedTlogs);

		}

	}

	public static void runTask2() throws FileNotFoundException {

		EPRuntime runtime = initializeEngineForTask2(mostProfArea);
		CellHelper chelper = new CellHelper(DebsMain.FIRST_CELL_X, DebsMain.FIRST_CELL_Y, DebsMain.SHIFT_X,
				DebsMain.SHIFT_Y, 300);
		List<TaxiLog> taxiLogs = null;

		EPOnDemandPreparedQueryParameterized updateNamedWindowQuery = runtime
				.prepareQueryWithParameters(getEplQuery(ONDEMAND_UPDATE_NAMED_WINDOW_QUERY));

		try (DataFileParser dataFileParser = new DataFileParser(DebsMain.DATA_FILE_URL, DebsMain.DELIMITER,
				DebsMain.columncount, chelper)) {

			taxiLogs = dataFileParser.parseNextLinesFromCSVGroupedByDropoffDate();
			long currentTime = DataFileParser.getCURRENT_TIME();
			long startingTime = DataFileParser.getCURRENT_TIME();

			while (currentTime - startingTime <= TEST_INTERVAL_IN_IN_MS) {
				runtime.sendEvent(new CurrentTimeEvent(currentTime));
				if (currentTime >= DataFileParser.getCURRENT_TIME()) {
					for (TaxiLog tlog : taxiLogs) {
						tlog.setInserted(System.currentTimeMillis());
						updateNamedWindowQuery.setObject(1, tlog.getHack_license());
						runtime.executeQuery(updateNamedWindowQuery);
						runtime.sendEvent(tlog);
					}
					taxiLogs = dataFileParser.parseNextLinesFromCSVGroupedByDropoffDate();
					// System.out.println(mostProfArea);
				}

				
			/*	if ((currentTime - startingTime) % (60*60*1000) == 0) {
					System.out.println(mostProfArea);
					System.out.println("currentTime:" + new Date(currentTime));
					
				}*/
				currentTime += 1000;

			}

		}

	}
}
