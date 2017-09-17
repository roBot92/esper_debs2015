package onlab.esper_debs2015_projekt;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.List;

import onlab.event.TaxiLog;
import onlab.main.DebsMain;
import onlab.positioning.CellHelper;
import onlab.utility.DataFileParser;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	List<TaxiLog> taxiLogs = null;
		CellHelper chelper = new CellHelper(DebsMain.FIRST_CELL_X, DebsMain.FIRST_CELL_Y,
				DebsMain.SHIFT_X.divide(BigDecimal.valueOf(2)),
				DebsMain.SHIFT_Y.divide(BigDecimal.valueOf(2)), 300);



		try {
			DataFileParser dataFileParser = new DataFileParser(DebsMain.DATA_FILE_URL, DebsMain.DELIMITER, DebsMain.columncount, chelper);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		

    }
}
