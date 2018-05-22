package hu.bme.mit.esper.main;

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

import hu.bme.mit.entities.TaxiLog;
import hu.bme.mit.esper.listener.Task1InsertedDelayListener;
import hu.bme.mit.esper.listener.task1.Task1Listener;
import hu.bme.mit.esper.listener.task2.Task2CountOfEmptyTaxesListener;
import hu.bme.mit.esper.listener.task2.Task2InsertedDelayListener;
import hu.bme.mit.esper.listener.task2.Task2MedianListener;
import hu.bme.mit.positioning.CellHelper;
import hu.bme.mit.toplist.FrequentRoutesToplistSet;
import hu.bme.mit.toplist.ProfitableAreaToplistSet;
import hu.bme.mit.toplist.ToplistSetInterface;
import hu.bme.mit.utility.DataFileParser;
import hu.bme.mit.utility.ExecutionSetup;
import hu.bme.mit.utility.PrintHelper;
public class App {

	public static String TASK1_QUERY = "C:\\Users\\Rózsavölgyi Botond\\git\\esper_debs2015\\esper_debs2015_projekt\\src\\main\\resources\\onlab\\esper_debs2015_projekt\\Task1CountOfRoutes.sql";
	public static String TASK_1_2_INSERTED_DELAY_QUERY = "C:\\Users\\Rózsavölgyi Botond\\git\\esper_debs2015\\esper_debs2015_projekt\\src\\main\\resources\\onlab\\esper_debs2015_projekt\\Task1_2InsertedDelayQuery.sql";
	//
	public static String MEDIAN_OF_CELL = "C:\\Users\\Rózsavölgyi Botond\\git\\esper_debs2015\\esper_debs2015_projekt\\src\\main\\resources\\onlab\\esper_debs2015_projekt\\Task2GetMedianByCell.sql";
	public static String NAMED_WINDOW_DECLARATION = "C:\\Users\\Rózsavölgyi Botond\\git\\esper_debs2015\\esper_debs2015_projekt\\src\\main\\resources\\onlab\\esper_debs2015_projekt\\Task2LocationNamedWindowDeclaration.sql";
	public static String COUNT_OF_EMPTY_TAXILOGS_BY_CELL = "C:\\Users\\Rózsavölgyi Botond\\git\\esper_debs2015\\esper_debs2015_projekt\\src\\main\\resources\\onlab\\esper_debs2015_projekt\\Task2CountOfValidTaxiLogLocations.sql";
	public static String NAMED_WINDOW_INSERTION_QUERY = "C:\\Users\\Rózsavölgyi Botond\\git\\esper_debs2015\\esper_debs2015_projekt\\src\\main\\resources\\onlab\\esper_debs2015_projekt\\Task2InsertIntoLocationNamedWindow.sql";
	public static String ONDEMAND_UPDATE_NAMED_WINDOW_QUERY = "C:\\Users\\Rózsavölgyi Botond\\git\\esper_debs2015\\esper_debs2015_projekt\\src\\main\\resources\\onlab\\esper_debs2015_projekt\\Task2OnDemandUpdateParametrizedQuery.sql";

	static FrequentRoutesToplistSet freqRouteToplist = new FrequentRoutesToplistSet();
	static ProfitableAreaToplistSet mostProfArea = new ProfitableAreaToplistSet();

	public static final long TEST_INTERVAL_IN_IN_MS = 31*24 * 60 * 60 * 1000l;
	public static final long BENCHMARK_FREQUENCY_IN_MS = 1000 * 60l;

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
		
		EPStatement statement = engine.getEPAdministrator().createEPL(getEplQuery(TASK1_QUERY));

		EPRuntime runtime = engine.getEPRuntime();
		// Set to external clock
		runtime.sendEvent(new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_EXTERNAL));
		
		statement.addListener(new Task1Listener(toplist));
		
		EPStatement insertedStatement = engine.getEPAdministrator()
				.createEPL(getEplQuery(TASK_1_2_INSERTED_DELAY_QUERY));
		insertedStatement.addListener(new Task1InsertedDelayListener(toplist));

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
		runTask(freqRouteToplist, epRuntime, ExecutionSetup.task1ResultToCompareFileName, null,
				ExecutionSetup.OUTPUT_COOMPARING_MODE, 1);
		freqRouteToplist.clear();
		epRuntime = initializeEngineForTask1(freqRouteToplist);
		runTask(freqRouteToplist, epRuntime, ExecutionSetup.task1MemoryMeasuringResultFileName, null,
				ExecutionSetup.MEMORY_MEASURING_MODE, 1);
		freqRouteToplist.clear();
		epRuntime = initializeEngineForTask1(freqRouteToplist);
		runTask(freqRouteToplist, epRuntime, ExecutionSetup.task1TimeMeasuringResultFileName, null,
				ExecutionSetup.TIME_MEASURING_MODE, 1);

	}

	public static void runTask2() throws IOException {

		EPRuntime epRuntime = initializeEngineForTask2(mostProfArea);
		EPOnDemandPreparedQueryParameterized updateNamedWindowQuery = epRuntime
				.prepareQueryWithParameters(getEplQuery(ONDEMAND_UPDATE_NAMED_WINDOW_QUERY));
		runTask(mostProfArea, epRuntime, ExecutionSetup.task2ResultToCompareFileName, updateNamedWindowQuery,
				ExecutionSetup.OUTPUT_COOMPARING_MODE, 2);
		mostProfArea.clear();
		epRuntime = initializeEngineForTask2(mostProfArea);
		runTask(mostProfArea, epRuntime, ExecutionSetup.task2MemoryMeasuringResultFileName, updateNamedWindowQuery,
				ExecutionSetup.MEMORY_MEASURING_MODE, 2);
		mostProfArea.clear();
		epRuntime = initializeEngineForTask2(mostProfArea);
		runTask(mostProfArea, epRuntime, ExecutionSetup.task2TimeMeasuringResultFileName, updateNamedWindowQuery,
				ExecutionSetup.TIME_MEASURING_MODE, 2);

	}

	public static void runTask(ToplistSetInterface toplist, EPRuntime epRuntime, String fileName,
			EPOnDemandPreparedQueryParameterized onDemandQuery, int runningMode, int divisor) throws IOException {
		CellHelper chelper = new CellHelper(ExecutionSetup.FIRST_CELL_X, ExecutionSetup.FIRST_CELL_Y,
				ExecutionSetup.SHIFT_X.divide(BigDecimal.valueOf(divisor)),
				ExecutionSetup.SHIFT_Y.divide(BigDecimal.valueOf(divisor)), 300 * divisor);
		List<TaxiLog> taxiLogs = null;

		PrintHelper.restartCurrentTime();
		System.gc();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return;
		}
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
		PrintHelper.restartCurrentTime();
		try (DataFileParser dataFileParser = new DataFileParser(ExecutionSetup.DATA_FILE_URL, ExecutionSetup.DELIMITER,
				ExecutionSetup.columncount, chelper)) {
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

				previousToplistWithoutDelay = PrintHelper.handlePrintActions(toplist, runningMode,
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
