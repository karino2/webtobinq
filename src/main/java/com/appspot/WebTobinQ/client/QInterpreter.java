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
	}
	
	public QInterpreter(Writable console) {
		_curEnv = _rootEnv = new Environment(null);
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
		if(term.getType() == QParser.SYMBOL)
			return _curEnv.get(term.getText());
		if(term.getType() == QParser.XXFUNCALL)
			return evalCallFunction(term);
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
	void assignToFormalList(Tree subList, Tree formalList,
			Environment funcEnv) {
		QObject args = evalSubList(subList);
		// may be we should use another symbol, but use this for a while.
		funcEnv.put("...", args);
	}

	// (XXSUBLIST (XXSUB1 1) (XXSUB1 2))
	// Currently, tmp implementation.
	QObject evalSubList(Tree subList) {
		QObject args = new QObject("numeric");
		for(int i = 0; i < subList.getChildCount(); i++)
		{
			QObject arg = evalTerm(subList.getChild(i).getChild(0));
			args.set(i, arg);
		}
		return args;
	}

	QObject evalPlus(QObject arg1, QObject arg2)
	{
		return QObject.createInt(((int)(Integer)arg1.getValue())+(int)(Integer)arg2.getValue());
	}

	public QObject evalBinary(Tree op, Tree arg1, Tree arg2) {
		if("+".equals(op.getText()))
		{
			QObject term1 = evalTerm(arg1);
			QObject term2 = evalTerm(arg2);
			return evalPlus(term1, term2);
		}
		throw new RuntimeException("NYI");
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
