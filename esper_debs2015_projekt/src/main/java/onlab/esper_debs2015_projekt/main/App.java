package onlab.esper_debs2015_projekt.main;

import java.math.BigDecimal;
import java.util.List;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;

import onlab.event.TaxiLog;
import onlab.main.DebsMain;
import onlab.positioning.CellHelper;
import onlab.utility.DataFileParser;

/**
 * Hello world!
 *
 */
public class App {
	private static String testQuery = "select hack_license from TaxiLog#time(30 sec)";

	public static void main(String[] args) throws Exception {

		List<TaxiLog> taxiLogs = null;
		CellHelper chelper = new CellHelper(DebsMain.FIRST_CELL_X, DebsMain.FIRST_CELL_Y,
				DebsMain.SHIFT_X.divide(BigDecimal.valueOf(2)), DebsMain.SHIFT_Y.divide(BigDecimal.valueOf(2)), 300);

		EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();
		try (DataFileParser dataFileParser = new DataFileParser(DebsMain.DATA_FILE_URL, DebsMain.DELIMITER,
				DebsMain.columncount, chelper)) {

			engine.getEPAdministrator().getConfiguration().addEventType(TaxiLog.class);
			EPStatement statement = engine.getEPAdministrator().createEPL(testQuery);

			statement.addListener((newData, oldData) -> {
				String res = (String) newData[0].get("hack_license");
				System.out.println(oldData);
			});

			for (int i = 0; i < 100; i++) {
				taxiLogs = dataFileParser.parseNextLinesFromCSVGroupedByDropoffDate();
				for (TaxiLog tlog : taxiLogs) {
					engine.getEPRuntime().sendEvent(tlog);
				}

			}
			

		} finally {
			engine.destroy();
		}

	}
}
