package com.appspot.WebTobinQ.client;

import java.util.ArrayList;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;

import com.appspot.WebTobinQ.client.TableRetrievable.RetrieveArgument;
import com.googlecode.gchart.client.GChart;
import com.googlecode.gchart.client.GChart.Axis;

public class QFunction extends QObject {
	Tree _body;
	Tree _formalList;
	
	public final String ARGNAME = "__arg__";
	public static Tree parseFormalList(String code)
	{
		CharStream codes = new ANTLRStringStream(code);
		QLexer lex = new QLexer(codes);
		CommonTokenStream tokens = new CommonTokenStream(lex);
		QParser parser = new QParser(tokens);
		try {
			return (Tree)parser.formlist().getTree();
		} catch (RecognitionException e) {
			// This input is statically determined.
			// Never come here if once it passed.
			e.printStackTrace();
		}
		return null;
	}
	
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
				QObject args = funcEnv.get(ARGNAME);
				QObjectBuilder bldr = new QObjectBuilder();
				for(int i = 0; i < args.getLength(); i++)
				{
					// should validate args here.
					bldr.add(args.get(i));
				}
				return bldr.result();
				
			}
		};
	}
	
	// "seq"
	public static QFunction createSeq()
	{
		return new QFunction(parseFormalList("beg, end, by=1"), null) {
			public boolean isPrimitive() {return true; }
			public QObject callPrimitive(Environment funcEnv, QInterpreter intp)
			{
				QObject begO = funcEnv.get("beg");
				QObject endO = funcEnv.get("end");
				if(begO == null || endO == null)
					throw new RuntimeException("seq argument seems wrong");
				double beg = begO.getDouble();
				double end = endO.getDouble();
				
				QObject res = new QObject("numeric");
				for(int i = 0; i <= end-beg; i++)
				{
					res.set(i, QObject.createNumeric(beg+i));
				}
				return res;				
			}
		};
	}
	
	public static Plotable _plotable;

	// "plot"
	public static QObject createPlot(Plotable plotable) {
		_plotable = plotable;
		return new QFunction(parseFormalList("x, y, main=NULL, xlim=NULL, ylim=NULL"), null) {
			public boolean isPrimitive() {return true; }
			public QObject callPrimitive(Environment funcEnv, QInterpreter intp)
			{
				QObject x = funcEnv.get("x");
				QObject y = funcEnv.get("y");
				QObject ylim = funcEnv.get("ylim");
				// String is NYI...
				// QObject main = funcEnv.get("main");
				if(x.getLength() != y.getLength())
					throw new RuntimeException("x, y length differ");
				GChart chart = _plotable.getChart();
				chart.clearCurves();
				
				if(ylim != QObject.Null)
				{
					if(ylim.getLength() != 2)
						throw new RuntimeException("ylim is not 2 element object");
					Axis axis = chart.getYAxis();
					axis.setAxisMin(ylim.get(0).getDouble());
					axis.setAxisMax(ylim.get(1).getDouble());
				}

				/*
				if(main != QObject.Null)
					chart.setChartTitle((String)main.getValue());
					*/
			    chart.setChartSize(350, 250);
			    chart.addCurve();
				GChart.Curve curve = chart.getCurve();
			    addPoints(x, y, curve);
			    // chart.getCurve().setLegendLabel("x, y");
			    chart.getXAxis().setAxisLabel("x");
			    chart.getYAxis().setAxisLabel("y");
			    
			    _plotable.showChart();
			    
				return QObject.Null;
			}
		};
	}
	
	public static QObject createLines(Plotable plotable) {
		// Share to createPlot. Be careful!
		_plotable = plotable;
		return new QFunction(parseFormalList("x, y=NULL"), null) {
			public boolean isPrimitive() {return true; }
			public QObject callPrimitive(Environment funcEnv, QInterpreter intp)
			{
				QObject x = funcEnv.get("x");
				QObject y = funcEnv.get("y");
				if(QObject.Null.equals(y))
					throw new RuntimeException("lines: y=NULL, NYI");
				if(x.getLength() != y.getLength())
					throw new RuntimeException("lines: x, y length differ");
				GChart chart = _plotable.getChart();
				chart.addCurve();
				GChart.Curve curve = chart.getCurve();
		        curve.getSymbol().setSymbolType(GChart.SymbolType.LINE);
			    addPoints(x, y, curve);
				
			    _plotable.showChart();
				return QObject.Null;
			}			
		};
	}
	private static void addPoints(QObject x, QObject y, GChart.Curve curve) {
		for (int i = 0; i < x.getLength(); i++)
		{
			double x1 = x.get(i).getDouble();
			double y1 = y.get(i).getDouble();
			curve.addPoint(x1, y1);
		}
	}
	
	public static double sumObj(QObject obj)
	{
		int len = obj.getLength();
		double sum = 0;
		for(int i = 0; i < len; i++)
		{
			sum += obj.get(i).getDouble();
		}
		return sum;
	}
	
	public static QFunction createVar()
	{
		return new QFunction(null, null){
			public boolean isPrimitive() {return true; }
			public QObject callPrimitive(Environment funcEnv, QInterpreter intp)
			{
				QObject args = funcEnv.get(ARGNAME);
				if(args.getLength() != 1)
					throw new RuntimeException("Argument of var should be 1");
				QObject arg = args.get(0);
				int len = arg.getLength();
				double mean = sumObj(arg)/len;
				
				double var = 0;
				for(int i = 0; i < len; i++)
				{
					double x = arg.get(i).getDouble();
					var += (x - mean)*(x-mean);
				}
				var = var/(len-1);
				return QObject.createNumeric(var);
				
			}
		};
	}

	
	
	public static QFunction createMean()
	{
		return new QFunction(null, null){
			public boolean isPrimitive() {return true; }
			public QObject callPrimitive(Environment funcEnv, QInterpreter intp)
			{
				QObject args = funcEnv.get(ARGNAME);
				if(args.getLength() != 1)
					throw new RuntimeException("Argument of mean should be 1");
				QObject arg = args.get(0);
				int len = arg.getLength();
				double mean = sumObj(arg)/len;
				return QObject.createNumeric(mean);
				
			}
		};
	}


	public static QFunction createSqrt()
	{
		return new QFunction(parseFormalList("x"), null){
			public boolean isPrimitive() {return true; }
			public QObject callPrimitive(Environment funcEnv, QInterpreter intp)
			{
				QObject args = funcEnv.get("x");
				int len = args.getLength();
				if(len == 1)
					return QObject.createNumeric(Math.sqrt(args.getDouble()));
				QObjectBuilder bldr = new QObjectBuilder();
				for(int i = 0; i < len; i++)
				{
					QObject x = QObject.createNumeric(Math.sqrt(args.get(i).getDouble()));
					bldr.add(x);
				}
				return bldr.result();
			}
		};
	}
	
	public static QFunction createDataFrame()
	{
		return new QFunction(null, null){
			public boolean isPrimitive() {return true; }
			public QObject callPrimitive(Environment funcEnv, QInterpreter intp)
			{
				QObject arg = funcEnv.get(ARGNAME);
				return QList.createDataFrameFromVector(arg);
			}
		};
	}
	
	public static TableRetrievable _retrievable;
	
	public static QFunction createReadServer(TableRetrievable retrievable)
	{
		_retrievable = retrievable;
		return new QFunction(parseFormalList("url, table, fields, range, num=10"), null) {
			public boolean isPrimitive() {return true; }
			public QObject callPrimitive(Environment funcEnv, QInterpreter intp)
			{
				QObject url = funcEnv.get("url");
				QObject tableName = funcEnv.get("table");
				QObject fieldsQ = funcEnv.get("fields");
				QObject range = funcEnv.get("range");
				QObject num = funcEnv.get("num");
				
				ArrayList<String> fields = qobjectToFieldsQ(fieldsQ);

				RetrieveArgument arg;
				
				if(null == range)
					arg = new RetrieveArgument();
				else {
					QObject fname = range.get(0);
					QObject beg = range.get(1);
					QObject end = range.get(2);
					arg = new RetrieveArgument(fname.getValue().toString(),
							beg.getDouble(), end.getDouble(), num.getInt());
				}
				
				JSONTable table = _retrievable.retrieve(url.getValue().toString(),
						tableName.getValue().toString(),
						fields, arg);
				
				return QList.createDataFrameFromJSONTable(table);
			}
			ArrayList<String> qobjectToFieldsQ(QObject fieldsQ) {
				ArrayList<String> ret = new ArrayList<String>();
				for(int i = 0; i < fieldsQ.getLength(); i++) {
					ret.add(fieldsQ.get(i).getValue().toString());
				}
				return ret;
			}
			
		};
	}
	public String toString()
	{
		return "function ...";
	}

}
