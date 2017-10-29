package onlab.esper_deps2015_projekt.listeners;

import java.util.Date;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import onlab.event.Route;
import onlab.positioning.Cell;
import onlab.utility.FrequentRoutesToplistSet;

public class Task1Listener implements UpdateListener {

	private FrequentRoutesToplistSet toplist = new FrequentRoutesToplistSet();

	private static String FREQUENCY = "frequency";
	private static String PICKUP_CELL = "pickup_cell";
	private static String DROPOFF_CELL = "dropoff_cell";
	private static String LAST_INSERTED = "last_inserted";

	public Task1Listener(FrequentRoutesToplistSet toplist) {
		this.toplist = toplist;
	}

	@Override
	public void update(EventBean[] newData, EventBean[] oldData) {

		if (newData != null) {
			for (EventBean taxiLogBean : newData) {
				toplist.refreshRoute((Cell) taxiLogBean.get(PICKUP_CELL), (Cell) taxiLogBean.get(DROPOFF_CELL),
						(Date) taxiLogBean.get(LAST_INSERTED), ((Long) taxiLogBean.get(FREQUENCY)).intValue());
			}
		}

	}

}
