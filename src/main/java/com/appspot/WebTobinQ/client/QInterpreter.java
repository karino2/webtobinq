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
	}
	
	public QInterpreter(Writable console) {
		this(console, null);
	}	
	
	
	Plotable _plotable;
	public QInterpreter(Writable console, Plotable plotable) {
		_curEnv = _rootEnv = new Environment(null);
		_plotable = plotable;
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
	
	public QObject eval(String codestext)
	{
		QObject ret = null;
		try {
			Tree tree = buildTree(codestext);
			debugPrint(tree.toStringTree());
			ret = evalTree(tree);
			if(ret != null)
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
		QObject ret = null;
		if(t.getChildCount() == 0)
			return ret;
		return evalTerm(t.getChild(0));
	}
	
	public QObject evalTerm(Tree term)
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
			return evalCallFunction(term);
		if(term.getType() == QParser.SYMBOL)
			return _curEnv.get(term.getText());
		if(term.getType() == QParser.NULL_CONST)
			return QObject.Null;
		System.out.println(term.getType());
		throw new RuntimeException("NYI2:" + term.getType());
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
		return null;
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

		handleXXSymSub1(subList, funcEnv, assigned);
		handleXXSub(subList, formalList, funcEnv, assigned);		
		assignRemainingDefaultArguments(formalList, funcEnv, assigned);
	}

	private void assignRemainingDefaultArguments(Tree formalList, Environment funcEnv,
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
			funcEnv.put(sym.getText(), evalTerm(val));
		}
	}

	private void handleXXSub(Tree subList, Tree formalList,
			Environment funcEnv, HashMap<String, Boolean> assigned) {
		for(int i = 0; i < subList.getChildCount(); i++)
		{
			Tree node = subList.getChild(i);
			if(node.getType() != QParser.XXSUB1)
				continue;
			Tree formArg = getNextFormArgSym(formalList, assigned);
			Tree val = node.getChild(0);
			funcEnv.put(formArg.getText(), evalTerm(val));			
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

	private void handleXXSymSub1(Tree subList, Environment funcEnv,
			HashMap<String, Boolean> assigned) {
		for(int i = 0; i < subList.getChildCount(); i++)
		{
			Tree node = subList.getChild(i);
			if(node.getType() != QParser.XXSYMSUB1)
				continue;
			Tree sym = node.getChild(0);
			Tree val = node.getChild(1);
			funcEnv.put(sym.getText(), evalTerm(val));
			
			assigned.put(sym.getText(), true);
		}
	}

	private void assignToDefaultArgs(Tree subList, Environment funcEnv) {
		QObject args = new QObject("list");
		for(int i = 0; i < subList.getChildCount(); i++)
		{
			QObject arg = evalTerm(subList.getChild(i).getChild(0));
			args.set(i, arg);
		}
		funcEnv.put(ARGNAME, args);
	}

	public static double getDouble(QObject o)
	{
		return QFunction.getDouble(o);
	}

	QObject evalPlus(QObject arg1, QObject arg2)
	{
		if(arg1.getLength() == 1 &&arg2.getLength() == 1)
			return QObject.createNumeric(getDouble(arg1)+getDouble(arg2));
		QObject ret = new QObject(arg1.getMode());
		QObject r1 = arg1;
		QObject r2 = arg2;
		if(r1.getLength() > r2.getLength())
			r2 = arg2.recycle(r1.getLength());
		else if(r1.getLength() < r2.getLength())
			r1 = arg1.recycle(r2.getLength());	
		for(int i = 0; i < r1.getLength(); i++)
		{
			double i1 = getDouble(r1.get(i));
			double i2 = getDouble(r2.get(i));
			ret.set(i, QObject.createNumeric(i1+i2));
		}
		return ret;
	}


	public QObject evalBinary(Tree op, Tree arg1, Tree arg2) {
		if(QParser.LEFT_ASSIGN == op.getType() ||
				QParser.EQ_ASSIGN == op.getType())
		{
			if(arg1.getType() != QParser.SYMBOL) {
				debugPrint("lvalue not symbol, throw");
				throw new RuntimeException("lvalue of assign is not SYMBOL, NYI");
			}
			_curEnv.put(arg1.getText(), evalTerm(arg2));
			return null;
		}
		QObject term1 = evalTerm(arg1);
		QObject term2 = evalTerm(arg2);
		if("+".equals(op.getText()))
		{
			return evalPlus(term1, term2);
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
}
