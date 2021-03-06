package com.appspot.WebTobinQ.client;

import java.util.ArrayList;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.appspot.WebTobinQ.client.ForestNode.Edge;
import com.appspot.WebTobinQ.client.ForestNode.Traversable;
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
	
	public QObject QClone() {
		return this;
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
					QObject obj = args.get(i);
					if(obj.getLength() == 1)
						bldr.add(obj);
					else
					{
						for(int j = 0; j < obj.getLength(); j++)
						{
							bldr.add(obj.get(j));
						}
					}
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
		return new QFunction(parseFormalList("x, y, main=NULL, xlim=NULL, ylim=NULL, type=\"p\""), null) {
			public boolean isPrimitive() {return true; }
			public QObject callPrimitive(Environment funcEnv, QInterpreter intp)
			{
				QObject x = funcEnv.get("x");
				QObject y = funcEnv.get("y");
				QObject ylim = funcEnv.get("ylim");
				QObject main = funcEnv.get("main");
				if(x.getLength() != y.getLength())
					throw new RuntimeException("x, y length differ");
				_plotable.resetChart();
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

				if(main != QObject.Null)
					chart.setChartTitle((String)main.getValue());
				
			    chart.setChartSize(350, 250);
			    chart.addCurve();
				GChart.Curve curve = chart.getCurve();
			    addPoints(x, y, curve);
			    // chart.getCurve().setLegendLabel("x, y");
			    chart.getXAxis().setAxisLabel("x");
			    chart.getXAxis().setTickCount(9);
			    chart.getXAxis().setTicksPerLabel(2);
			    chart.getYAxis().setAxisLabel("y");

			    QObject typ = funcEnv.get("type");
			    if ((String)typ.getValue() == "o"  ||
			    		(String)typ.getValue() == "l" ||
			    		(String)typ.getValue() == "b")
			    	curve.getSymbol().setSymbolType(GChart.SymbolType.LINE);
			    
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
	
	public static class QObjectForestAdapter {
		QObjectForestAdapter _parent;
		QObject _self;
		int _childIndex;
		public QObjectForestAdapter(QObjectForestAdapter parent, QObject slf, int childIndex) {
			_parent = parent;
			_self = slf;
			_childIndex = childIndex;
		}
		public QObjectForestAdapter getParent()
		{
			return _parent;
		}
	}
	
	public static ForestIterater<QObjectForestAdapter> createForestIterater(QObject rootObj) {
		QObjectForestAdapter rootAda = new QObjectForestAdapter(null, rootObj, 0);
		ForestNode<QObjectForestAdapter> root = new ForestNode<QObjectForestAdapter>(new Traversable<QObjectForestAdapter>(){

			public QObjectForestAdapter getChild(QObjectForestAdapter elem,
					int i) {
				// this is not child for some case. but I guess it's ok for this case.
				QObject child = elem._self.get(i);
				return new QObjectForestAdapter(elem, child, i);
			}

			public QObjectForestAdapter getParent(QObjectForestAdapter elem) {
				return elem.getParent();
			}

			public int getChildCount(QObjectForestAdapter elem) {
				QObject obj = elem._self;
				if(obj.getMode() == QList.LIST_TYPE)
					return obj.getLength();
				// in this case, obj is leaf
				if(obj.getLength() == 1)
					return 0;
				return obj.getLength();
			}

			public int getChildIndex(QObjectForestAdapter elem) {
				return elem._childIndex;
			}
			
		}, ForestNode.Edge.Leading, rootAda);
		return new ForestIterater<QObjectForestAdapter>(root);
	}

	// sum
	public static QFunction createSum()
	{
		return new QFunction(null, null){
			public boolean isPrimitive() {return true; }
			public QObject callPrimitive(Environment funcEnv, QInterpreter intp)
			{
				QObject args = funcEnv.get(ARGNAME);
				double sum = 0;
				ForestIterater<QObjectForestAdapter> iter = createForestIterater(args);
				while(iter.hasNext())
				{
					ForestNode<QObjectForestAdapter> node = iter.next();
					if(node.getEdge() != Edge.Trailing)
						continue;
					QObject obj = node.getElement()._self;
					if(obj.getLength() != 1 ||
							obj.getMode() == QList.LIST_TYPE)
						continue;
					sum += obj.getDouble();
				}
				return QObject.createNumeric(sum);
				
			}
		};
	}

	// cumsum
	public static QFunction createCumulativeSum()
	{
		return new QFunction(null, null){
			public boolean isPrimitive() {return true; }
			public QObject callPrimitive(Environment funcEnv, QInterpreter intp)
			{
				QObject args = funcEnv.get(ARGNAME);
				if(args.getLength() != 1)
					throw new RuntimeException("Argument of cumsum should be 1");
				QObjectBuilder bldr = new QObjectBuilder();
				QObject arg = args.get(0);
				int len = arg.getLength();
				double cum = 0;
				for(int i = 0; i < len; i++)
				{
					QObject obj = arg.get(i);
					cum += obj.getDouble();
					bldr.add(QObject.createNumeric(cum));
				}
				return bldr.result();				
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
	
	public static QFunction createList()
	{
		return new QFunction(null, null){
			public boolean isPrimitive() {return true; }
			public QObject callPrimitive(Environment funcEnv, QInterpreter intp)
			{
				QObject arg = funcEnv.get(ARGNAME);
				return arg; // arg is QList. in this case, I think I don't need QClone().
			}
		};
	}
	public static QFunction createLength()
	{
		return new QFunction(null, null){
			public boolean isPrimitive() {return true; }
			public QObject callPrimitive(Environment funcEnv, QInterpreter intp)
			{
				QObject args = funcEnv.get(ARGNAME);
				if(args.getLength() != 1)
					throw new RuntimeException("Argument of length should be 1");
				QObject arg = args.get(0);
				return QObject.createNumeric(arg.getLength());
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
					arg = new RetrieveArgument(num.getInt());
				else {
					QObject beg = range.get(0);
					QObject end = range.get(1);
					arg = new RetrieveArgument(
							beg.getDouble(), end.getDouble(), num.getInt());
				}
				
				JSONTable table = _retrievable.retrieve(url.getValue().toString(),
						tableName.getValue().toString(),
						fields, arg);
				
				return QList.createDataFrameFromJSONTable(table);
			}
			ArrayList<String> qobjectToFieldsQ(QObject fieldsQ) {
				ArrayList<String> ret = new ArrayList<String>();
				if(fieldsQ == null)
					return ret;
				for(int i = 0; i < fieldsQ.getLength(); i++) {
					ret.add(fieldsQ.get(i).getValue().toString());
				}
				return ret;
			}
			
		};
	}
	
	// attributes
	public static QFunction createAttributes()
	{
		return new QFunction(parseFormalList("obj"), null){
			public boolean isPrimitive() {return true; }
			public QObject callPrimitive(Environment funcEnv, QInterpreter intp)
			{
				QObject arg = funcEnv.get("obj");
				if(arg.isNull())
					throw new RuntimeException("Argument of attributes should be one object");
				return arg.attributesAsList();
				
			}
		};
	}
	
	// is.null
	public static QFunction createIsNull()
	{
		return new QFunction(parseFormalList("obj"), null){
			public boolean isPrimitive() {return true; }
			public QObject callPrimitive(Environment funcEnv, QInterpreter intp)
			{
				QObject arg = funcEnv.get("obj");
				if(QObject.Null.equals(arg))
					return QObject.TRUE;
				return QObject.FALSE;
			}
		};
	}

	// as.numeric
	public static QFunction createAsNumeric()
	{
		return new QFunction(parseFormalList("obj"), null){
			public boolean isPrimitive() {return true; }
			public QObject callPrimitive(Environment funcEnv, QInterpreter intp)
			{
				QObject arg = funcEnv.get("obj");
				return asNumeric(arg);				
			}
		};
	}
	
	static QObject asNumeric(QObject arg) {
		if(arg.isNull())
			throw new RuntimeException("Argument of as.numeric should be one object");
		if(QObject.CHARACTER_TYPE.equals(arg.getMode())) {
			QObjectBuilder bldr = new QObjectBuilder();
			for(int i = 0; i < arg.getLength(); i++) {
				bldr.add(QObject.createNumeric(Double.valueOf((String)arg.get(i).getValue())));
			}
			return bldr.result();
		}
		if(QList.DATAFRAME_CLASS.equals(arg.getQClass()))
		{
			QList dfArg = (QList)arg;
			QObjectBuilder bldr = new QObjectBuilder();
			for(int i = 0; i < dfArg.getLength(); i++)
			{
				QObject col = dfArg.getBBInt(i);
				if(col.getLength() != 1)
					throw new RuntimeException("as.numeric: unsupported data.frame dimension.");
				if(QObject.CHARACTER_TYPE.equals(col.getMode()))
					bldr.add(QObject.createNumeric(Double.valueOf((String)col.getValue())));
				else if(QObject.NUMERIC_TYPE.equals(col.getMode()))
					bldr.add(QObject.createNumeric((Double)col.getValue()));
				else
					throw new RuntimeException("as.numeric: unsupported df element type");
			}
			return bldr.result();
		}
		throw new RuntimeException("as.numeric: unsupported argument");
	}
	
	// match.arg
	public static QFunction createMatchArg()
	{
		return new QFunction(parseFormalList("arg, choices, several.ok = FALSE"), null){
			public boolean isPrimitive() {return true; }
			public QObject callPrimitive(Environment funcEnv, QInterpreter intp)
			{
				QObject arg = funcEnv.get("arg");
				QObject choices = funcEnv.get("choices");
				if(arg.getLength() > 1)
					throw new RuntimeException("first argument of match.arg is not scalar.");
				if(choices == null)
				{
					Tree orgVal = funcEnv.getSexp("arg");
					if(orgVal.getType() != QParser.SYMBOL)
						throw new RuntimeException("match.arg, arg's caller sexp is not symbol. what situation? :" + orgVal.toStringTree());
					choices = funcEnv.getDefaultValue(orgVal.getText());
				}
				String argStr = (String)arg.getValue();
				for(int i = 0; i < choices.getLength(); i++)
				{
					String target = (String)choices.get(i).getValue();
					if(target.startsWith(argStr))
						return choices.get(i);
				}
				throw new RuntimeException("[arg] should match to one of the member of [choices]");
			}
		};
	}
	
	// substitute
	public static QFunction createSubstitute()
	{
		return new QFunction(parseFormalList("expr, env"), null){
			public boolean isPrimitive() {return true; }
			public QObject callPrimitive(Environment funcEnv, QInterpreter intp)
			{
				Tree sexp = funcEnv.getSexp("expr");
				if(sexp.getType() == QParser.SYMBOL)
				{
					sexp = handleSymbol(funcEnv, sexp);
				}
				else
				{
					ForestIterater<Tree> iter = QInterpreter.createIterater(sexp);
					while(iter.hasNext())
					{
						ForestNode<Tree> node = iter.next();
						if(node.getEdge() == Edge.Trailing)
							continue;
						Tree t = node.getElement();
						if(t.getType() == QParser.SYMBOL)
						{
							Tree newSexp = handleSymbol(funcEnv, t);
							if(newSexp != t)
							{
								t.getParent().replaceChildren(t.getChildIndex(), t.getChildIndex(), newSexp);
							}
							continue;
						}
					}
				}

				return QObject.createCall(funcEnv, sexp);
			}
			private Tree handleSymbol(Environment funcEnv, Tree sexp) {
				Tree newSexp = funcEnv.getSexp(sexp.getText());
				if(newSexp != null)
					sexp = newSexp;
				return sexp;
			}
		};
	}

	// deparse
	public static QFunction createDeParse()
	{
		return new QFunction(parseFormalList("obj"), null){
			public boolean isPrimitive() {return true; }
			public QObject callPrimitive(Environment funcEnv, QInterpreter intp)
			{
				QObject arg = funcEnv.get("obj");
				if(arg.getMode() != "call")
					throw new RuntimeException("not supported deparse with non-expression arg");
				
				StringBuffer buf = new StringBuffer();
				Tree sexp = arg.getSexp();
				recursivePrint(sexp, buf);
				return QObject.createCharacter(buf.toString());
			}
			
			private void recursivePrint(Tree sexp, StringBuffer buf) {
				if(sexp.getType() == QParser.XXBINARY)
				{
					recursivePrint(sexp.getChild(1), buf);
					buf.append(sexp.getChild(0).getText());
					recursivePrint(sexp.getChild(2), buf);
					return;
				}
				if(sexp.getType() == QParser.SYMBOL)
				{
					buf.append(sexp.getText());
					return;
				}
				if(sexp.getType() == QParser.STR_CONST)
				{
					buf.append("\"");
					buf.append(sexp.getText());
					buf.append("\"");
					return;
				}
				if(sexp.getType() == QParser.DecimalLiteral)
				{
					buf.append(sexp.getText());
					return;
				}
			}
		};
	}

	public String toString()
	{
		return "function ...";
	}

}
