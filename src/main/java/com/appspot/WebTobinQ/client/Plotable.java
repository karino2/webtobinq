package com.appspot.WebTobinQ.client;

import com.googlecode.gchart.client.GChart;

public interface Plotable {

	public abstract GChart getChart();
	public abstract void resetChart();

	public abstract void showChart();

}