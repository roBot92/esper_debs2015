package onlab.esper_deps2015_projekt.listeners;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import onlab.event.AreaWithProfit;
import onlab.utility.ProfitableAreaToplistSet;

public class Task2MedianListener implements UpdateListener {

	private ProfitableAreaToplistSet<AreaWithProfit> mostProfArea;

	
	
	public Task2MedianListener(ProfitableAreaToplistSet<AreaWithProfit> mostProfArea) {
		this.mostProfArea = mostProfArea;
	}



	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		
	}

}
