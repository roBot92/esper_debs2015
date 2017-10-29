package onlab.esper_deps2015_projekt.listeners;

import java.util.Date;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import onlab.positioning.Cell;
import onlab.utility.ProfitableAreaToplistSet;

public class Task2CountOfEmptyTaxesListener implements UpdateListener {

	private ProfitableAreaToplistSet mostProfArea = new ProfitableAreaToplistSet();

	private static String COUNT = "countOfTaxes";
	private static String CELL = "cell";
	private static String LAST_INSERTED = "lastTime";

	public Task2CountOfEmptyTaxesListener(ProfitableAreaToplistSet mostProfArea) {
		this.mostProfArea = mostProfArea;
	}

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		if (newEvents != null) {
			for (EventBean event : newEvents) {
				mostProfArea.refreshAreaTaxiCount((Cell) event.get(CELL), (Date) event.get(LAST_INSERTED),
						(Long) event.get(COUNT));
			}
		}

	}

}
