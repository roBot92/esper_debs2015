package onlab.esper_debs2015_projekt.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

import com.espertech.esper.client.EPOnDemandPreparedQueryParameterized;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esper.client.time.TimerControlEvent;

import onlab.esper_deps2015_projekt.listeners.Task1InsertedDelayListener;
import onlab.esper_deps2015_projekt.listeners.Task1Listener;
import onlab.esper_deps2015_projekt.listeners.Task2CountOfEmptyTaxesListener;
import onlab.esper_deps2015_projekt.listeners.Task2InsertedDelayListener;
import onlab.esper_deps2015_projekt.listeners.Task2MedianListener;
import onlab.event.TaxiLog;
import onlab.main.DebsMain;
import onlab.positioning.CellHelper;
import onlab.utility.DataFileParser;
import onlab.utility.FrequentRoutesToplistSet;
import onlab.utility.ProfitableAreaToplistSet;
import onlab.utility.ToplistSetInterface;

public class App {

	static String TASK1_QUERY = "C:\\Users\\Rózsavölgyi Botond\\git\\esper_debs2015\\esper_debs2015_projekt\\src\\main\\resources\\onlab\\esper_debs2015_projekt\\Task1Query.sql";
	static String TASK_1_2_INSERTED_DELAY_QUERY = "C:\\Users\\Rózsavölgyi Botond\\git\\esper_debs2015\\esper_debs2015_projekt\\src\\main\\resources\\onlab\\esper_debs2015_projekt\\Task1_2InsertedDelayQuery.sql";
	//
	static String MEDIAN_OF_CELL = "C:\\Users\\Rózsavölgyi Botond\\git\\esper_debs2015\\esper_debs2015_projekt\\src\\main\\resources\\onlab\\esper_debs2015_projekt\\Task2GetMedianByCell.sql";
	static String NAMED_WINDOW_DECLARATION = "C:\\Users\\Rózsavölgyi Botond\\git\\esper_debs2015\\esper_debs2015_projekt\\src\\main\\resources\\onlab\\esper_debs2015_projekt\\Task2LocationNamedWindowDeclaration.sql";
	static String COUNT_OF_EMPTY_TAXILOGS_BY_CELL = "C:\\Users\\Rózsavölgyi Botond\\git\\esper_debs2015\\esper_debs2015_projekt\\src\\main\\resources\\onlab\\esper_debs2015_projekt\\Task2CountOfValidTaxiLogLocations.sql";
	static String NAMED_WINDOW_INSERTION_QUERY = "C:\\Users\\Rózsavölgyi Botond\\git\\esper_debs2015\\esper_debs2015_projekt\\src\\main\\resources\\onlab\\esper_debs2015_projekt\\Task2InsertIntoLocationNamedWindow.sql";
	static String ONDEMAND_UPDATE_NAMED_WINDOW_QUERY = "C:\\Users\\Rózsavölgyi Botond\\git\\esper_debs2015\\esper_debs2015_projekt\\src\\main\\resources\\onlab\\esper_debs2015_projekt\\Task2OnDemandUpdateParametrizedQuery.sql";

	static FrequentRoutesToplistSet freqRouteToplist = new FrequentRoutesToplistSet();
	static ProfitableAreaToplistSet mostProfArea = new ProfitableAreaToplistSet();

	public static final long TEST_INTERVAL_IN_IN_MS = 2 * 60 * 60 * 1000;
	public static final long BENCHMARK_FREQUENCY_IN_MS = 1000 * 60;

	public static void main(String[] args) {

		try {
			runTask1();
			runTask2();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static EPRuntime initializeEngineForTask1(FrequentRoutesToplistSet toplist) {
		EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();
		engine.initialize();
		engine.getEPAdministrator().getConfiguration().addEventType(TaxiLog.class);
		EPStatement insertedStatement = engine.getEPAdministrator()
				.createEPL(getEplQuery(TASK_1_2_INSERTED_DELAY_QUERY));
		EPStatement statement = engine.getEPAdministrator().createEPL(getEplQuery(TASK1_QUERY));

		EPRuntime runtime = engine.getEPRuntime();
		// Set to external clock
		runtime.sendEvent(new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_EXTERNAL));
		insertedStatement.addListener(new Task1InsertedDelayListener(toplist));
		statement.addListener(new Task1Listener(toplist));

		return runtime;
	}

	public static EPRuntime initializeEngineForTask2(ProfitableAreaToplistSet toplist) {

		EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();
		engine.initialize();
		engine.getEPAdministrator().getConfiguration().addEventType(TaxiLog.class);

		engine.getEPAdministrator().createEPL(getEplQuery(NAMED_WINDOW_DECLARATION));
		engine.getEPAdministrator().createEPL(getEplQuery(NAMED_WINDOW_INSERTION_QUERY));
		engine.getEPAdministrator().createEPL("create index hlIndex on TaxiLogLocationWindow(hack_license)");

		EPStatement medianStatement = engine.getEPAdministrator().createEPL(getEplQuery(MEDIAN_OF_CELL));
		medianStatement.addListener(new Task2MedianListener(toplist));
		EPStatement countOfEmptyTaxesStatement = engine.getEPAdministrator()
				.createEPL(getEplQuery(COUNT_OF_EMPTY_TAXILOGS_BY_CELL));
		countOfEmptyTaxesStatement.addListener(new Task2CountOfEmptyTaxesListener(toplist));

		EPStatement insertedStatement = engine.getEPAdministrator()
				.createEPL(getEplQuery(TASK_1_2_INSERTED_DELAY_QUERY));
		insertedStatement.addListener(new Task2InsertedDelayListener(toplist));
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

	public static void runTask1() throws IOException {
		EPRuntime epRuntime = initializeEngineForTask1(freqRouteToplist);
		runTask(freqRouteToplist, epRuntime, DebsMain.task1ResultToCompareFileName, null,
				DebsMain.OUTPUT_COOMPARING_MODE, 1);
		freqRouteToplist.clear();
		epRuntime = initializeEngineForTask1(freqRouteToplist);
		runTask(freqRouteToplist, epRuntime, DebsMain.task1MemoryMeasuringResultFileName, null,
				DebsMain.MEMORY_MEASURING_MODE, 1);
		freqRouteToplist.clear();
		epRuntime = initializeEngineForTask1(freqRouteToplist);
		runTask(freqRouteToplist, epRuntime, DebsMain.task2TimeMeasuringResultFileName, null,
				DebsMain.TIME_MEASURING_MODE, 1);

	}

	public static void runTask2() throws IOException {

		EPRuntime epRuntime = initializeEngineForTask2(mostProfArea);
		EPOnDemandPreparedQueryParameterized updateNamedWindowQuery = epRuntime
				.prepareQueryWithParameters(getEplQuery(ONDEMAND_UPDATE_NAMED_WINDOW_QUERY));
		runTask(mostProfArea, epRuntime, DebsMain.task2ResultToCompareFileName, updateNamedWindowQuery,
				DebsMain.OUTPUT_COOMPARING_MODE, 2);
		mostProfArea.clear();
		epRuntime = initializeEngineForTask2(mostProfArea);
		runTask(mostProfArea, epRuntime, DebsMain.task2MemoryMeasuringResultFileName, updateNamedWindowQuery,
				DebsMain.MEMORY_MEASURING_MODE, 2);
		mostProfArea.clear();
		epRuntime = initializeEngineForTask2(mostProfArea);
		runTask(mostProfArea, epRuntime, DebsMain.task2TimeMeasuringResultFileName, updateNamedWindowQuery,
				DebsMain.TIME_MEASURING_MODE, 2);

	}

	public static void runTask(ToplistSetInterface toplist, EPRuntime epRuntime, String fileName,
			EPOnDemandPreparedQueryParameterized onDemandQuery, int runningMode, int divisor) throws IOException {
		CellHelper chelper = new CellHelper(DebsMain.FIRST_CELL_X, DebsMain.FIRST_CELL_Y,
				DebsMain.SHIFT_X.divide(BigDecimal.valueOf(divisor)),
				DebsMain.SHIFT_Y.divide(BigDecimal.valueOf(divisor)), 300 * divisor);
		List<TaxiLog> taxiLogs = null;

		Runtime runtime = Runtime.getRuntime();
		File resultFile = null;
		BufferedWriter resultFileWriter = null;

		resultFile = new File(fileName);
		if (resultFile.exists()) {
			resultFile.delete();
		}
		try {
			resultFileWriter = new BufferedWriter(new FileWriter(resultFile));
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		String previousToplistWithoutDelay = null;
		DebsMain.restartCurrentTime();
		try (DataFileParser dataFileParser = new DataFileParser(DebsMain.DATA_FILE_URL, DebsMain.DELIMITER,
				DebsMain.columncount, chelper)) {
			taxiLogs = dataFileParser.parseNextLinesFromCSVGroupedByDropoffDate();
			long currentTime = DataFileParser.getCURRENT_TIME();
			long startingTime = DataFileParser.getCURRENT_TIME();

			long countOfProcessedTlogs = 0;
			while (currentTime - startingTime <= TEST_INTERVAL_IN_IN_MS) {
				epRuntime.sendEvent(new CurrentTimeEvent(currentTime));
				if (currentTime >= DataFileParser.getCURRENT_TIME()) {
					for (TaxiLog tlog : taxiLogs) {
						tlog.setInserted(System.currentTimeMillis());
						if (onDemandQuery != null && tlog.getPickup_cell() != null && tlog.getDropoff_cell() != null) {
							onDemandQuery.setObject(1, tlog.getHack_license());
							epRuntime.executeQuery(onDemandQuery);
						}
						epRuntime.sendEvent(tlog);
						countOfProcessedTlogs++;
					}
					taxiLogs = dataFileParser.parseNextLinesFromCSVGroupedByDropoffDate();
				}

				previousToplistWithoutDelay = DebsMain.handlePrintActions(toplist, runningMode,
						previousToplistWithoutDelay, resultFileWriter, currentTime, countOfProcessedTlogs, startingTime,
						BENCHMARK_FREQUENCY_IN_MS, runtime);
				currentTime += 1000;
			}
		} finally {
			if (resultFileWriter != null) {
				resultFileWriter.close();
			}
		}
	}
}
