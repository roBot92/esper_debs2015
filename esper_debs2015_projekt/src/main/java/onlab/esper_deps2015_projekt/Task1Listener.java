package onlab.esper_deps2015_projekt;

import java.util.Date;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import onlab.event.Route;
import onlab.positioning.Cell;
import onlab.utility.FrequentRoutesToplistSet;

public class Task1Listener implements UpdateListener {

	private FrequentRoutesToplistSet<Route> toplist = new FrequentRoutesToplistSet<Route>();

	
	public Task1Listener(FrequentRoutesToplistSet<Route> toplist) {
		super();
		this.toplist = toplist;
	}


	@Override
	public void update(EventBean[] newData, EventBean[] oldData) {

		/*StringBuilder result = new StringBuilder(newData[0].get("frequency").toString());
		result.append("\t");
		result.append(newData[0].get("pickup_cell").toString());
		result.append("\t");
		result.append(newData[0].get("dropoff_cell").toString());
		result.append("\t");
		result.append(newData[0].get("last_inserted").toString());
		System.out.println(result);*/
		for(EventBean taxiLogBean : newData) {
			toplist.refreshRoute((Cell)taxiLogBean.get("pickup_cell"), (Cell)taxiLogBean.get("dropoff_cell"), 
								(Date)taxiLogBean.get("last_inserted"),  ((Long)taxiLogBean.get("frequency")).intValue());
		}
		
		
		

	}

}
