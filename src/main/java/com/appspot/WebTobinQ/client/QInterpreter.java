package com.appspot.WebTobinQ.client;

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
	
	public Object eval(String codestext)
	{
		Object ret = null;
		try {
			Tree tree = buildTree(codestext);
			debugPrint(tree.toStringTree());
			ret = evalTree(tree);
			println(ret.toString());
		} catch (RecognitionException e) {
			debugPrint("#parse Error!");
			_console.write(e.toString());
			e.printStackTrace();
		}
		return ret;
	}

	Object evalTree(Tree tree) {
		Object ret = null;
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
		if(term.getType() == QParser.DecimalLiteral)
			return QObject.createInt(Integer.valueOf(term.getText()));
		if(term.getType() == QParser.XXBINARY) // recursive call now.
			return evalBinary(term.getChild(0), term.getChild(1), term.getChild(2));
		if(term.getType() == QParser.XXFUNCALL)
			return evalCallFunction(term);
		if(term.getType() == QParser.SYMBOL)
			return _curEnv.get(term.getText());
		System.out.println(term.getType());
		throw new RuntimeException("NYI");
	}

	// (XXFUNCALL c (XXSUBLIST (XXSUB1 1) (XXSUB1 2)))
	QObject evalCallFunction(Tree term) {
		QObject funcCand = (QObject)_curEnv.get(term.getChild(0).getText());
		if(funcCand.getMode() == "function")
		{
			QFunction func = (QFunction)funcCand;
			Environment outer = _curEnv;
			_curEnv = new Environment(_curEnv);
			assignToFormalList(term.getChild(1), func.getFormalList(), _curEnv);
			QObject ret;
			if(func.isPrimitive())
				ret = func.callPrimitive(_curEnv, this);
			else
				ret = evalValue(func.getBody());
			_curEnv = outer;
			return ret;
		}
		// error handling, can I use exception in JS env?
		_console.write("right value of func call is not function. After investigate exception, I'll handle.");
		return null;
	}
	
	// (XXSUBLIST (XXSUB1 1) (XXSUB1 2))
	// currently, I just ignore formal List for primitive function.
	void assignToFormalList(Tree subList, Tree formalList,
			Environment funcEnv) {
		QObject args = evalSubList(subList);
		// may be we should use another symbol, but use this for a while.
		funcEnv.put("...", args);
	}

	// (XXSUBLIST (XXSUB1 1) (XXSUB1 2))
	// Currently, tmp implementation.
	QObject evalSubList(Tree subList) {
		QObject args = new QObject("list");
		for(int i = 0; i < subList.getChildCount(); i++)
		{
			QObject arg = evalTerm(subList.getChild(i).getChild(0));
			args.set(i, arg);
		}
		return args;
	}

	QObject evalPlus(QObject arg1, QObject arg2)
	{
		if(arg1.getLength() == 1 &&arg2.getLength() == 1)
			return QObject.createInt(((Integer)arg1.getValue())+(Integer)arg2.getValue());
		QObject ret = new QObject(arg1.getMode());
		QObject r1 = arg1;
		QObject r2 = arg2;
		if(r1.getLength() > r2.getLength())
			r2 = arg2.recycle(r1.getLength());
		else if(r1.getLength() < r2.getLength())
			r1 = arg1.recycle(r2.getLength());	
		for(int i = 0; i < r1.getLength(); i++)
		{
			int i1 = (Integer)r1.get(i).getValue();
			int i2 = (Integer)r2.get(i).getValue();
			ret.set(i, QObject.createInt(i1+i2));
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
			QObject args = new QObject(term1.getMode());
			args.set(0, term1);
			args.set(1, term2);
			funcEnv.put("...", args);
			return ((QFunction)_curEnv.get("seq")).callPrimitive(funcEnv, this);
		}
		else 		throw new RuntimeException("NYI");
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
