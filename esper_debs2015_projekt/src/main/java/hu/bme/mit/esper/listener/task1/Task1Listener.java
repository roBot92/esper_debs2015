package hu.bme.mit.esper.listener.task1;

import java.util.Date;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import hu.bme.mit.positioning.Cell;
import hu.bme.mit.toplist.FrequentRoutesToplistSet;

public class Task1Listener implements UpdateListener {

	private FrequentRoutesToplistSet toplist = new FrequentRoutesToplistSet();

	private static final String FREQUENCY = "frequency";
	private static final String PICKUP_CELL = "pickup_cell";
	private static final String DROPOFF_CELL = "dropoff_cell";
	private static final String LAST_INSERTED = "max_dropoff_datetime";
	
	

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
