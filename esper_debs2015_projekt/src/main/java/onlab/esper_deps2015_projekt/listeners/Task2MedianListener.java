package onlab.esper_deps2015_projekt.listeners;

import java.util.Date;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import onlab.positioning.Cell;
import onlab.utility.ProfitableAreaToplistSet;

public class Task2MedianListener implements UpdateListener {

	private ProfitableAreaToplistSet mostProfArea;
	private static String MEDIAN = "med";
	private static String CELL = "pickup_cell";
	private static String LAST_INSERTED = "lastInserted";

	public Task2MedianListener(ProfitableAreaToplistSet mostProfArea) {
		this.mostProfArea = mostProfArea;
	}

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		if (newEvents != null) {
			for (EventBean event : newEvents) {
				mostProfArea.refreshAreaMedian((Cell) event.get(CELL), (Date) event.get(LAST_INSERTED),
						(Double) event.get(MEDIAN));
			}
		}

	}

}
