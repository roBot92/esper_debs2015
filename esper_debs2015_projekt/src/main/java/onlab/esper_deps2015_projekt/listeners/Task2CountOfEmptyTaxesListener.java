package onlab.esper_deps2015_projekt.listeners;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import onlab.event.AreaWithProfit;
import onlab.utility.ProfitableAreaToplistSet;

public class Task2CountOfEmptyTaxesListener implements UpdateListener {

	private ProfitableAreaToplistSet<AreaWithProfit> mostProfArea = new ProfitableAreaToplistSet<AreaWithProfit>();

	
	public Task2CountOfEmptyTaxesListener(ProfitableAreaToplistSet<AreaWithProfit> mostProfArea) {
		this.mostProfArea = mostProfArea;
	}


	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		// TODO Auto-generated method stub

	}

}
