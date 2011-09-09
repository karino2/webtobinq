package com.appspot.WebTobinQ.client;

import java.util.HashMap;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;
import com.appspot.WebTobinQ.client.ForestNode.Edge;

public class QInterpreter {
	
	Writable _console;	
	public Writable getConsole(){ return _console; }
	Environment _curEnv;
	Environment _rootEnv;
	
	public void registerBuiltinFunction()
	{
		_curEnv.put("c", QFunction.createConcatinate());
		_curEnv.put("seq", QFunction.createSeq());
		_curEnv.put("plot", QFunction.createPlot(_plotable));
		_curEnv.put("lines", QFunction.createLines(_plotable));
		_curEnv.put("mean", QFunction.createMean());
		_curEnv.put("length", QFunction.createLength());
		_curEnv.put("var", QFunction.createVar());
		_curEnv.put("sqrt", QFunction.createSqrt());
		_curEnv.put("data.frame", QFunction.createDataFrame());
		_curEnv.put("list", QFunction.createList());
		_curEnv.put("read.server", QFunction.createReadServer(_retrievable));
	}
	
	public QInterpreter(Writable console) {
		this(console, null, null);
	}	
	
	
	Plotable _plotable;
	TableRetrievable _retrievable;
	public QInterpreter(Writable console, Plotable plotable, TableRetrievable retrievable) {
		_curEnv = _rootEnv = new Environment(null);
		_plotable = plotable;
		_retrievable = retrievable;
		this._console = console;
		registerBuiltinFunction();
	}
	
	public void println(String str)
	{
		_console.write(str + "\n");
	}
	
	void debugPrint(String str)
	{
		println("<deb#>" + str);
	}
	
	Tree _lastTree;
	public QObject eval(String codestext)
	{
		QObject ret = null;
		try {
			_lastTree = buildTree(codestext);
			debugPrint(_lastTree.toStringTree());
			ret = evalTree(_lastTree);
			if(!QObject.Null.equals(ret))
				println(ret.toString());
		} catch (RecognitionException e) {
			debugPrint("#parse Error!");
			_console.write(e.toString());
			e.printStackTrace();
		}
		return ret;
	}

	QObject evalTree(Tree tree) {
		QObject ret = null;
		ForestIterater iter = new ForestIterater(tree);
		while(iter.hasNext())
		{
			ForestNode node = iter.next();
			if(node.getEdge() == Edge.Trailing)
				continue;
			Tree t = node.getElement();
			if(t.getType() == QParser.XXVALUE)
			{
				ret = evalValue(t);
				iter.skipChildren();
			}
		}
		return ret;
	}

	// (XXVALUE (XXBINARY + 2 3)), evalValue seems bad name.
	public QObject evalValue(Tree t) {
		QObject ret = QObject.Null;
		if(t.getChildCount() == 0)
			return ret;
		return evalExpr(t.getChild(0));
	}
	
	public QObject evalExpr(Tree term)
	{
		// R use numeric for most of the case (instead of int).
		if(term.getType() == QParser.DecimalLiteral)
			return QObject.createNumeric(Double.valueOf(term.getText()));
//			return QObject.createInt(Integer.valueOf(term.getText()));
		if(term.getType() == QParser.FloatingPointLiteral)
			return QObject.createNumeric(Double.valueOf(term.getText()));
		if(term.getType() == QParser.XXBINARY) // recursive call now.
			return evalBinary(term.getChild(0), term.getChild(1), term.getChild(2));
		if(term.getType() == QParser.XXFUNCALL)
		{
			try
			{
				return evalCallFunction(term);
			}
			catch(BlockException e)
			{
				Tree xxval = getParentXXValue(term);
				throw new BlockException(xxval);
			}
		}
		if(term.getType() == QParser.SYMBOL)
			return _curEnv.get(term.getText());
		if(term.getType() == QParser.NULL_CONST)
			return QObject.Null;		
		if(term.getType() == QParser.TRUE)
			return QObject.TRUE;
		if(term.getType() == QParser.FALSE)
			return QObject.FALSE;
		if(term.getType() == QParser.XXSUBSCRIPT)
			return evalSubscript(term);
		if(term.getType() == QParser.XXPAREN)
		{
			if(term.getChildCount() != 1)
				throw new RuntimeException("child num of XXPAREN is not 1. Which case?");
			return evalExpr(term.getChild(0));
		}
		// TODO: handle literal more seriously.
		if(term.getType() == QParser.STR_CONST)
			return QObject.createCharacter(term.getText().substring(1, term.getText().length()-1));
		System.out.println(term.getType());
		throw new RuntimeException("NYI2:" + term.getType());
	}

	private Tree getParentXXValue(Tree start) {
		Tree cur = start;
		while(cur != null && cur.getType() != QParser.XXVALUE )
			cur = cur.getParent();
		if(cur == null)
			throw new RuntimeException("Invalid AST tree. no XXVALUE in parent.");
		return cur;
	}

	// (XXSUBSCRIPT '[' lexpr sublist)
	// or (XXSUBSCRIPT LBB lexpr sublist)
	QObject evalSubscript(Tree term) {
		if(term.getChild(0).getType() == QParser.LBB)
			return evalSubscriptBB(term);
		return evalSubscriptBracket(term);
	}
	
	// (XXSUBSCRIPT [[ df (XXSUBLIST (XXSUB1 "x")))
	QObject evalSubscriptBB(Tree term) {
		QObject lexpr = evalExpr(term.getChild(1));
		Tree sublistTree = term.getChild(2);
		if(sublistTree.getChildCount() > 1)
			throw new RuntimeException("[[]] with multi dimentional, what's happend?");
		if(sublistTree.getChild(0).getType() != QParser.XXSUB1)
			throw new RuntimeException("Sublist with assign: gramatically accepted, but what situation?");
		QObject index = evalExpr(sublistTree.getChild(0).getChild(0));
		return lexpr.getBB(index);
	}

	private QObject evalSubscriptBracket(Tree term) {
		QObject lexpr = evalExpr(term.getChild(1));
		Tree sublistTree = term.getChild(2);
		if(sublistTree.getChildCount() > 1)
			throw new RuntimeException("NYI: multi dimentional array");
		if(sublistTree.getChild(0).getType() != QParser.XXSUB1)
			throw new RuntimeException("Sublist with assign: gramatically accepted, but what situation?");
		QObject range = evalExpr(sublistTree.getChild(0).getChild(0));
		if(range.getMode() == "logical")
			return subscriptByLogical(lexpr, range);
		return subscriptByNumber(lexpr, range);
	}
	
	private QObject subscriptByLogical(QObject lexpr, QObject range) {
		if(range.getLength() != lexpr.getLength())
			throw new RuntimeException("subscriptByLogical: length of logical list and lexpr is different");
		QObjectBuilder bldr = new QObjectBuilder();
		for(int i = 0; i < range.getLength(); i++)
		{
			QObject bool = range.get(i);
			if(QObject.TRUE.equals(bool))
				bldr.add(lexpr.get(i));
				
		}
		return bldr.result();
	}

	private QObject subscriptByNumber(QObject lexpr, QObject range) {
		if(range.getLength () == 1)
		{
			int  index = range.getInt();
			return lexpr.get(index-1);
		}
		QObjectBuilder bldr = new QObjectBuilder();
		for(int i = 0; i < range.getLength(); i++)
		{
			int index = range.get(i).getInt();
			QObject q = lexpr.get(index-1);
			bldr.add(q);
		}
		return bldr.result();
	}


	// (XXFUNCALL c (XXSUBLIST (XXSUB1 1) (XXSUB1 2)))
	QObject evalCallFunction(Tree term) {
		QObject funcCand = (QObject)_curEnv.get(term.getChild(0).getText());
		if(funcCand.getMode() == "function")
		{
			QFunction func = (QFunction)funcCand;
			Environment outer = _curEnv;
			_curEnv = new Environment(_curEnv);
			QObject ret;
			try 
			{
				assignToFormalList(term.getChild(1), func.getFormalList(), _curEnv);
				if(func.isPrimitive())
					ret = func.callPrimitive(_curEnv, this);
				else
					ret = evalValue(func.getBody());
			}
			finally
			{
				_curEnv = outer;
			}
			return ret;
		}
		// error handling, can I use exception in JS env?
		_console.write("right value of func call is not function. After investigate exception, I'll handle.");
		return QObject.Null;
	}
	
	public final String ARGNAME = "__arg__";
	
	// (XXSUBLIST (XXSUB1 1) (XXSYMSUB1 beg 4))
	// (XXFORMALLIST (XXFORMAL0 beg) (XXFORMAL1 end 10))
	void assignToFormalList(Tree subList, Tree formalList,
			Environment funcEnv) {
		if(formalList == null) {
			assignToDefaultArgs(subList, funcEnv);
			return;
		}
		HashMap<String, Boolean> assigned = new HashMap<String, Boolean>();

		// (..., x=y, ...)
		handleXXSymSub1(subList, funcEnv, assigned);

		// (..., x, ...)
		handleXXSub(subList, formalList, funcEnv, assigned);
		
		assignRemainingDefaultArguments(formalList, funcEnv, assigned);
	}

	void assignRemainingDefaultArguments(Tree formalList, Environment funcEnv,
			HashMap<String, Boolean> assigned) {
		for(int i = 0; i < formalList.getChildCount(); i++)
		{
			Tree formArg = formalList.getChild(i);
			if(formArg.getType() != QParser.XXFORMAL1)
				continue;
			Tree sym = formArg.getChild(0);
			if(isAssigned(sym.getText(), assigned))
				continue;
			Tree val = formArg.getChild(1);
			funcEnv.put(sym.getText(), evalExpr(val));
		}
	}

	void handleXXSub(Tree subList, Tree formalList,
			Environment funcEnv, HashMap<String, Boolean> assigned) {
		for(int i = 0; i < subList.getChildCount(); i++)
		{
			Tree node = subList.getChild(i);
			if(node.getType() != QParser.XXSUB1)
				continue;
			Tree formArg = getNextFormArgSym(formalList, assigned);
			Tree val = node.getChild(0);
			funcEnv.put(formArg.getText(), evalExpr(val));			
			assigned.put(formArg.getText(), true);
		}
	}
	
	private boolean isAssigned(String name, HashMap<String, Boolean> assigned)
	{
		return assigned.containsKey(name) && assigned.get(name);
	}

	// slow
	// (XXFORMALLIST (XXFORMAL0 beg) (XXFORMAL1 end 10))
	private Tree getNextFormArgSym(Tree formalList,
			HashMap<String, Boolean> assigned) {
		for(int i = 0; i < formalList.getChildCount(); i++)
		{
			Tree arg = formalList.getChild(i);
			Tree sym = arg.getChild(0);
			if(isAssigned(sym.getText(), assigned))
				continue;
			return sym;
		}
		throw new RuntimeException("Too much arguments");
    }

	void handleXXSymSub1(Tree subList, Environment funcEnv,
			HashMap<String, Boolean> assigned) {
		for(int i = 0; i < subList.getChildCount(); i++)
		{
			Tree node = subList.getChild(i);
			if(node.getType() != QParser.XXSYMSUB1)
				continue;
			Tree sym = node.getChild(0);
			Tree val = node.getChild(1);
			QObject arg = evalExpr(val).QClone();
			arg.setAttribute("names", QObject.createCharacter(sym.getText()));
			funcEnv.put(sym.getText(), arg);
			
			assigned.put(sym.getText(), true);
		}
	}

	private void assignToDefaultArgs(Tree subList, Environment funcEnv) {
		QObject args = new QList();
		int argNum = 0;
		for(int i = 0; i < subList.getChildCount(); i++)
		{
			Tree subObj = subList.getChild(i).getChild(0);
			QObject arg = evalExpr(subList.getChild(i).getChild(0));
			if(subObj.getType() == QParser.SYMBOL)
				arg.setAttribute("names", QObject.createCharacter(subObj.getText()));
			args.set(argNum++, arg);
		}
		funcEnv.put(ARGNAME, args);
	}

	interface doubleBinaryOperable {
		QObject execute(double i, double j);
	}
	
	QObject evalBinaryDouble(QObject arg1, QObject arg2, doubleBinaryOperable op)
	{
		if(arg1.getLength() == 1 &&arg2.getLength() == 1)
			return op.execute(arg1.getDouble(),arg2.getDouble());
		QObjectBuilder bldr = new QObjectBuilder();
		QObject r1 = arg1;
		QObject r2 = arg2;
		if(r1.getLength() > r2.getLength())
			r2 = arg2.recycle(r1.getLength());
		else if(r1.getLength() < r2.getLength())
			r1 = arg1.recycle(r2.getLength());	
		for(int i = 0; i < r1.getLength(); i++)
		{
			double i1 = r1.get(i).getDouble();
			double i2 = r2.get(i).getDouble();
			QObject q = op.execute(i1, i2);
			bldr.add(q);
		}
		return bldr.result();
		
	}

	QObject evalPlus(QObject arg1, QObject arg2)
	{
		return evalBinaryDouble(arg1, arg2, new doubleBinaryOperable() {
			public QObject execute(double i, double j) {
				return QObject.createNumeric(i+j);
			}
			
		});
	}

	QObject evalMinus(QObject arg1, QObject arg2)
	{
		return evalBinaryDouble(arg1, arg2, new doubleBinaryOperable() {
			public QObject execute(double i, double j) {
				return QObject.createNumeric(i-j);
			}
			
		});
	}
	QObject evalMultiple(QObject arg1, QObject arg2)
	{
		return evalBinaryDouble(arg1, arg2, new doubleBinaryOperable() {
			public QObject execute(double i, double j) {
				return QObject.createNumeric(i*j);
			}
			
		});
	}
	QObject evalDivide(QObject arg1, QObject arg2)
	{
		return evalBinaryDouble(arg1, arg2, new doubleBinaryOperable() {
			public QObject execute(double i, double j) {
				return QObject.createNumeric(i/j);
			}
			
		});
	}
	
	// <=
	QObject evalLE(QObject arg1, QObject arg2)
	{
		return evalBinaryDouble(arg1, arg2, new doubleBinaryOperable() {
			public QObject execute(double i, double j) {
				return QObject.createLogical(i <= j);
			}
			
		});
	}

	QObject evalGT(QObject arg1, QObject arg2)
	{
		return evalBinaryDouble(arg1, arg2, new doubleBinaryOperable() {
			public QObject execute(double i, double j) {
				return QObject.createLogical(i > j);
			}
			
		});
	}
	
	public QObject evalBinary(Tree op, Tree arg1, Tree arg2) {
		if(QParser.LEFT_ASSIGN == op.getType() ||
				QParser.EQ_ASSIGN == op.getType())
		{
			if(arg1.getType() != QParser.SYMBOL) {
				debugPrint("lvalue not symbol, throw");
				throw new RuntimeException("lvalue of assign is not SYMBOL, NYI");
			}
			_curEnv.put(arg1.getText(), evalExpr(arg2).QClone());
			return QObject.Null;
		}
		QObject term1 = evalExpr(arg1);
		QObject term2 = evalExpr(arg2);
		if("+".equals(op.getText()))
		{
			return evalPlus(term1, term2);
		}
		else if("-".equals(op.getText()))
		{
			return evalMinus(term1, term2);
		}
		else if("*".equals(op.getText()))
		{
			return evalMultiple(term1, term2);
		}
		else if("/".equals(op.getText()))
		{
			return evalDivide(term1, term2);
		}
		else if("%%".equals(op.getText()))
		{
			return evalBinaryDouble(term1, term2, new doubleBinaryOperable() {
				public QObject execute(double i, double j) {
					return QObject.createNumeric(i%j);
				}
			});
		}
		else if(QParser.LE == op.getType())
		{
			return evalLE(term1, term2);
		}
		else if(QParser.GT == op.getType())
		{
			return evalGT(term1, term2);
		}
		else if(":".equals(op.getText()))
		{
			Environment funcEnv = new Environment(_curEnv);
			// Be careful! I assume seq fomral name!
			funcEnv.put("beg", term1);
			funcEnv.put("end", term2);
			return ((QFunction)_curEnv.get("seq")).callPrimitive(funcEnv, this);
		}
		else 		throw new RuntimeException("NYI1");
	}

	private Tree buildTree(String codestext) throws RecognitionException {
		CharStream codes = new ANTLRStringStream(codestext);
		QLexer lex = new QLexer(codes);
		CommonTokenStream tokens = new CommonTokenStream(lex);
		QParser parser = new QParser(tokens);
	 	QParser.prog_return r = parser.prog();
		Tree tree = (Tree)r.getTree();
		return tree;
	}

	public QObject continueEval(Tree suspendedValue) {
		boolean reached = false;
		QObject ret = QObject.Null;
		ForestIterater iter = new ForestIterater(_lastTree);
		while(iter.hasNext())
		{
			ForestNode node = iter.next();
			if(node.getEdge() == Edge.Trailing)
				continue;
			Tree t = node.getElement();
			if(t.getType() == QParser.XXVALUE)
			{
				if(t == suspendedValue) {
					reached = true;
					ret = evalValue(t);
				} else if(reached){
					ret = evalValue(t);
				}
				iter.skipChildren();
			}
		}
		if(!QObject.Null.equals(ret))
			println(ret.toString());
		return ret;
	}
}
