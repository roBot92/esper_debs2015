package onlab.esper_deps2015_projekt.listeners;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import onlab.positioning.Cell;
import onlab.utility.FrequentRoutesToplistSet;

public class Task1InsertedDelayListener implements UpdateListener {

	private FrequentRoutesToplistSet toplist = new FrequentRoutesToplistSet();

	private static final String PICKUP_CELL = "pickup_cell";
	private static final String DROPOFF_CELL = "dropoff_cell";
	private static final String INSERTED_FOR_DELAY = "inserted_for_delay";

	public Task1InsertedDelayListener(FrequentRoutesToplistSet toplist) {
		this.toplist = toplist;
	}

	@Override
	public void update(EventBean[] newData, EventBean[] oldData) {

		if (newData != null) {
			for (EventBean taxiLogBean : newData) {
				toplist.refreshInsertedForDelay((Long) taxiLogBean.get(INSERTED_FOR_DELAY),
						(Cell) taxiLogBean.get(PICKUP_CELL), (Cell) taxiLogBean.get(DROPOFF_CELL));

			}
		}

	}

}
