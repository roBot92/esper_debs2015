package onlab.esper_debs2015_projekt.main;

import java.math.BigDecimal;
import java.util.List;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.StatementAwareUpdateListener;

import onlab.event.TaxiLog;
import onlab.main.DebsMain;
import onlab.positioning.CellHelper;
import onlab.utility.DataFileParser;

/**
 * Hello world!
 *
 */
public class App  {
	private static String testQuery = "select count(*) as frequency, pickup_cell, dropoff_cell from TaxiLog#time(30 min)"
			+ "where pickup_cell is not null and dropoff_cell is not null group by pickup_cell, dropoff_cell";

	public static void main(String[] args) throws Exception {

		List<TaxiLog> taxiLogs = null;
		CellHelper chelper = new CellHelper(DebsMain.FIRST_CELL_X, DebsMain.FIRST_CELL_Y,
				DebsMain.SHIFT_X.divide(BigDecimal.valueOf(2)), DebsMain.SHIFT_Y.divide(BigDecimal.valueOf(2)), 600);

		EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();
		try (DataFileParser dataFileParser = new DataFileParser(DebsMain.DATA_FILE_URL, DebsMain.DELIMITER,
				DebsMain.columncount, chelper)) {

			engine.getEPAdministrator().getConfiguration().addEventType(TaxiLog.class);
			EPStatement statement = engine.getEPAdministrator().createEPL(testQuery);

			statement.addListener((newData, oldData) -> {
				StringBuilder result = new StringBuilder(newData[0].get("frequency").toString());
				result.append("\t");
				result.append(newData[0].get("pickup_cell").toString());
				result.append("\t");
				result.append(newData[0].get("dropoff_cell").toString());
				
				System.out.println(result);
			});

			for (int i = 0; i < 10000; i++) {
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
