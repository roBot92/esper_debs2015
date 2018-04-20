package hu.bme.mit.esper.listener.task2;



import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import hu.bme.mit.positioning.Cell;
import hu.bme.mit.toplist.ToplistSetInterface;

public class Task2InsertedDelayListener implements UpdateListener {

	private ToplistSetInterface mostProfArea;
	private static final String PICKUP_CELL = "pickup_cell";
	private static final String DROPOFF_CELL = "dropoff_cell";
	private static final String INSERTED_FOR_DELAY = "inserted_for_delay";

	public Task2InsertedDelayListener(ToplistSetInterface mostProfArea) {
		this.mostProfArea = mostProfArea;
	}

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		if (newEvents != null) {
			for (EventBean event : newEvents) {
				mostProfArea.refreshInsertedForDelay((Long) event.get(INSERTED_FOR_DELAY),
						(Cell) event.get(PICKUP_CELL));
				mostProfArea.refreshInsertedForDelay((Long) event.get(INSERTED_FOR_DELAY),
						(Cell) event.get(DROPOFF_CELL));

			}
		}

	}

}
