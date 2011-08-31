package com.appspot.WebTobinQ.client;

import org.antlr.runtime.tree.Tree;

import com.googlecode.gchart.client.GChart;

public class QFunction extends QObject {
	Tree _body;
	Tree _formalList;
	
	public QFunction(Tree formalList, Tree body)
	{
		super("function");
		_formalList = formalList;
		_body = body;
	}
	
	public Tree getFormalList() { return _formalList; }
	public Tree getBody() { return _body; }
	
	public boolean isPrimitive() { return false; }
	public QObject callPrimitive(Environment funcEnv, QInterpreter intp)
	{
		return null;
	}

	// "c"
	public static QFunction createConcatinate()
	{
		return new QFunction(null, null){
			public boolean isPrimitive() {return true; }
			public QObject callPrimitive(Environment funcEnv, QInterpreter intp)
			{
				QObject args = funcEnv.get("...");
				// should validate args here.
				return args;
				
			}
		};
	}
	
	// "seq"
	public static QFunction createSeq()
	{
		return new QFunction(null, null) {
			public boolean isPrimitive() {return true; }
			public QObject callPrimitive(Environment funcEnv, QInterpreter intp)
			{
				QObject args = funcEnv.get("...");
				if(args.getLength() != 2)
					throw new RuntimeException("seq argument seems wrong");
				int beg = (Integer)args.get(0).getValue();
				int end = (Integer)args.get(1).getValue();
				
				QObject res = new QObject("numeric");
				for(int i = beg; i <= end; i++)
				{
					res.set(i-beg, QObject.createInt(i));
				}
				return res;				
			}
		};
	}
	
	public static Plotable _plotable;

	// "plot"
	public static QObject createPlot(Plotable plotable) {
		_plotable = plotable;
		return new QFunction(null, null) {
			public boolean isPrimitive() {return true; }
			public QObject callPrimitive(Environment funcEnv, QInterpreter intp)
			{
				QObject args = funcEnv.get("...");
				if(args.getLength() != 2)
					throw new RuntimeException("plot argument NYI");
				QObject x = args.get(0);
				QObject y = args.get(1);
				if(x.getLength() != y.getLength())
					throw new RuntimeException("x, y length differ");
				GChart chart = _plotable.getChart();
				chart.clearCurves();
				
			    // chart.setChartTitle("<b>x vs y</b>");
			    chart.setChartSize(350, 250);
			    chart.addCurve();
			    for (int i = 0; i < x.getLength(); i++)
			    {
			    	int x1 = (Integer)x.get(i).getValue();
			    	int y1 = (Integer)y.get(i).getValue();
			    	chart.getCurve().addPoint(x1, y1);
			    }
			    // chart.getCurve().setLegendLabel("x, y");
			    chart.getXAxis().setAxisLabel("x");
			    chart.getYAxis().setAxisLabel("y");
			    
			    _plotable.showChart();
			    
				return null;
			}
		};
	}
	
	public String toString()
	{
		return "function ...";
	}

}
