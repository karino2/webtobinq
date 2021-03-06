package com.appspot.WebTobinQ.client;

import java.util.HashMap;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;
import com.appspot.WebTobinQ.client.ForestNode.Edge;
import com.appspot.WebTobinQ.client.ForestNode.Traversable;

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
		_curEnv.put("sum", QFunction.createSum());
		_curEnv.put("cumsum", QFunction.createCumulativeSum());
		_curEnv.put("length", QFunction.createLength());
		_curEnv.put("var", QFunction.createVar());
		_curEnv.put("sqrt", QFunction.createSqrt());
		_curEnv.put("data.frame", QFunction.createDataFrame());
		_curEnv.put("list", QFunction.createList());
		_curEnv.put("read.server", QFunction.createReadServer(_retrievable));
		_curEnv.put("attributes", QFunction.createAttributes());
		_curEnv.put("as.numeric", QFunction.createAsNumeric());
		_curEnv.put("is.null", QFunction.createIsNull());
		_curEnv.put("substitute", QFunction.createSubstitute());
		_curEnv.put("deparse", QFunction.createDeParse());
		_curEnv.put("match.arg", QFunction.createMatchArg());
	}

	public QInterpreter(Writable console) {
		this(console, null, null);
	}	
	
	public void loadStaticScripts()
	{
		_console.write(ScriptResources.INSTANCE.hpfilter().getText());
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

	public static ForestIterater<Tree> createIterater(Tree root)
	{
		ForestNode<Tree> rootNode = new ForestNode<Tree>(new Traversable<Tree>() {

			public Tree getChild(Tree elem, int i) {
				return elem.getChild(i);
			}

			public Tree getParent(Tree elem) {
				return elem.getParent();
			}

			public int getChildCount(Tree elem) {
				return elem.getChildCount();
			}

			public int getChildIndex(Tree elem) {
				return elem.getChildIndex();
			}
			
		}, ForestNode.Edge.Leading, root);
		return new ForestIterater<Tree>(rootNode);
	}
	
	QObject evalTree(Tree tree) {
		QObject ret = null;
		ForestIterater<Tree> iter = createIterater(tree);
		while(iter.hasNext())
		{
			ForestNode<Tree> node = iter.next();
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
	
	interface Assignable {
		void assign(QObject rval);
	}
	
	abstract class DefaultAssign implements Assignable {

		Tree _term;
		public DefaultAssign(Tree term) { _term = term ;}		
	};
	
	public Assignable evalLexpr(Tree term)
	{
		if(term.getType() == QParser.SYMBOL) {
			return new DefaultAssign(term) {
				public void assign(QObject rval) {
					_curEnv.put(_term.getText(), rval);
				}				
			};
		}
		if(term.getType() == QParser.XXSUBSCRIPT)
		{
			// (XXSUBSCRIPT '[' lexpr sublist)
			// or (XXSUBSCRIPT LBB lexpr sublist)
			if(term.getChild(0).getType() == QParser.LBB)
				throw new RuntimeException("NYI: LBB assignment");
			return evalLexprForAssignSubscriptBracket(term);
			
		}
		throw new RuntimeException("assign: unsupported lexpr type(" + term.getType() + ")");
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
		// (XXEXPRLIST (XXBINARY <- b (XXBINARY * i 2)) (XXBINARY <- e (XXBINARY * i 13)))
		if(term.getType() == QParser.XXEXPRLIST)
		{
			QObject ret = QObject.Null;
			for(int i = 0; i < term.getChildCount(); i++)
			{
				ret = evalExpr(term.getChild(i));
			}
			return ret;
		}
		if(term.getType() == QParser.XXFOR)
			return evalFOR(term);
		// TODO: handle literal more seriously.
		if(term.getType() == QParser.STR_CONST)
			return QObject.createCharacter(term.getText().substring(1, term.getText().length()-1));
		// 		// (XXDEFUN XXFORMALLIST (XXEXPRLIST 1 2))
		if(term.getType() == QParser.XXDEFUN)
			return new QFunction(term.getChild(0), term.getChild(1));
		if(term.getType() == QParser.XXUNARY)
			return evalUnary(term);
		System.out.println(term.getType());
		throw new RuntimeException("NYI2:" + term.getType());
	}

	QObject evalUnary(Tree term) {
		// (XXUNARY - 3)
		if("-".equals(term.getChild(0).getText()))
		{
			QObject target = evalExpr(term.getChild(1));
			QObjectBuilder bldr = new QObjectBuilder();
			for(int i = 0; i < target.getLength(); i++) {
				bldr.add(QObject.createNumeric(-target.get(i).getDouble()));
			}
			return bldr.result();
		}
		throw new RuntimeException("NYI unary: " + term.getChild(0).getText());
	}

	// "(XXFOR (XXFORCOND i (XXBINARY : 1 10)) (XXEXPRLIST (XXBINARY <- b (XXBINARY * i 2)) (XXBINARY <- e (XXBINARY * i 13))))
	private QObject evalFOR(Tree term) {
		Tree forCond = term.getChild(0);
		Tree forExp = term.getChild(1);
		Tree sym = forCond.getChild(0);
		Tree forCondExp = forCond.getChild(1);
		if(sym.getType() != QParser.SYMBOL)
			throw new RuntimeException("argument of for expression is not symbol.");
		QObject condList = evalExpr(forCondExp);
		for(int i = 0; i < condList.getLength(); i++)
		{
			// for statements does not create env in R.
			_curEnv.put(sym.getText(), condList.get(i));
			evalExpr(forExp);
		}
		// for statements never return value in R (I think this is strange though).
		return QObject.Null;
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
	
	public class LogicalSubscriptAssigner implements Assignable {
		QObject _range;
		QObject _lval;
		
		public LogicalSubscriptAssigner(QObject lexpr, QObject range) {
			if(_lval.getLength() != _range.getLength())
				throw new RuntimeException("logical subscript, not the same length with arg and lexpr");
			_lval = lexpr;
			_range = range;
		}

		public void assign(QObject rvalList) {
			int rIndex = 0;
			for(int i = 0; i < _range.getLength(); i++){
				if(_range.get(i).isTrue())
				{
					if(rIndex >= rvalList.getLength())
						throw new RuntimeException("logical subscript assign: rval length is not equal to logical true num");
					_lval.set(i, rvalList.get(rIndex));
					rIndex++;
				}
			}
			if(rIndex != rvalList.getLength())
				throw new RuntimeException("logical subscript assign: rval length is not equal to logical true num");			
		}
	}
	
	public class NumberSubscriptAssigner implements Assignable {
		QObject _range;
		QObject _lval;
		
		public NumberSubscriptAssigner(QObject lexpr, QObject range) {
			_lval = lexpr;
			_range = range;
		}

		public void assign(QObject rvalList) {
			if(_range.getLength() != rvalList.getLength())
				throw new RuntimeException("number subscript: assignment of lval and rval is not the same length.");
			for(int i = 0; i < _range.getLength(); i++)
			{
				_lval.set(_range.get(i).getInt()-1, rvalList.get(i));
			}
		}
	}
	
	private Assignable evalLexprForAssignSubscriptBracket(Tree term) {
		QObject lexpr = evalExpr(term.getChild(1));
		Tree sublistTree = term.getChild(2);
		validateSubscriptBracket(sublistTree);
		if(sublistTree.getChildCount() > 1)
			throw new RuntimeException("NYI: assigment with multi dimensional subscript");
		
		QObject range = evalExpr(sublistTree.getChild(0).getChild(0));
		if(range.getMode() == "logical")
			return new LogicalSubscriptAssigner(lexpr, range);
		return new NumberSubscriptAssigner(lexpr, range);
		
	}

	private QObject evalSubscriptBracket(Tree term) {
		QObject lexpr = evalExpr(term.getChild(1));
		Tree sublistTree = term.getChild(2);
		validateSubscriptBracket(sublistTree);
		if(sublistTree.getChildCount() == 1)
		{
			QObject range = evalExpr(sublistTree.getChild(0).getChild(0));
			return lexpr.subscriptByOneArg(range);
		}
		else // sublistTree.getChildCount() == 2
		{
			Tree rangeRowNode = sublistTree.getChild(0);
			Tree rangeColNode = sublistTree.getChild(1);
			
			// a[1,] 
			// -> "(XXSUBSCRIPT [ a (XXSUBLIST (XXSUB1 1) XXSUB0))"
			if(rangeRowNode.getType() != QParser.XXSUB0 
					&& rangeColNode.getType() == QParser.XXSUB0)
			{
				QObject rangeRow = evalExpr(rangeRowNode.getChild(0));
				if(rangeRow.getMode() == "logical")
					throw new RuntimeException("NYI: multi dimensional subscript with logical array");
				return subscriptByRowNumber(lexpr, rangeRow);
			}
			// other case, NYI.
			throw new RuntimeException("NYI: multi dimensional subscript");
		}
	}

	private QObject subscriptByRowNumber(QObject lexpr, QObject rangeRow) {
		if(!lexpr.isDataFrame())
			throw new RuntimeException("NYI: multi dimensional subscript for none data frame");
		QList df = (QList) lexpr;
		return df.subscriptByRow(rangeRow);
	}

	private void validateSubscriptBracket(Tree sublistTree) {
		if(sublistTree.getChildCount() > 2)
			throw new RuntimeException("NYI: multi dimentional array more than 2");
		if(sublistTree.getChild(0).getType() != QParser.XXSUB1)
			throw new RuntimeException("Sublist with assign: gramatically accepted, but what situation?");
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
					ret = evalExprList(func.getBody());
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
		
	// (XXEXPRLIST 1 2)
	private QObject evalExprList(Tree body) {
		QObject ret = QObject.Null;
		for(int i = 0; i < body.getChildCount(); i++)
		{
			ret = evalExpr(body.getChild(i));
		}
		return ret;
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
		storeDefaultArg(formalList, funcEnv);
	}

	// (XXFORMALLIST (XXFORMAL0 beg) (XXFORMAL1 end 10))
	private void storeDefaultArg(Tree formalList, Environment funcEnv) {
		for(int i = 0; i < formalList.getChildCount(); i++)
		{
			Tree formArg = formalList.getChild(i);
			if(formArg.getType() != QParser.XXFORMAL1)
				continue;
			Tree sym = formArg.getChild(0);
			Tree val = formArg.getChild(1);
			funcEnv.putDefault(sym.getText(), evalExpr(val));
		}
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
			funcEnv.put(formArg.getText(), evalExpr(val), val);			
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
	
	interface binaryOperable {
		QObject execute(QObject i, QObject j);
	}


	interface doubleBinaryOperable {
		QObject execute(double i, double j);
	}
	
	QObject evalBinaryGeneric(QObject arg1, QObject arg2, binaryOperable op)
	{
		if(arg1.getLength() == 1 &&arg2.getLength() == 1)
			return op.execute(arg1,arg2);
		QObjectBuilder bldr = new QObjectBuilder();
		QObject r1 = arg1;
		QObject r2 = arg2;
		if(r1.getLength() > r2.getLength())
			r2 = arg2.recycle(r1.getLength());
		else if(r1.getLength() < r2.getLength())
			r1 = arg1.recycle(r2.getLength());	
		for(int i = 0; i < r1.getLength(); i++)
		{
			QObject q = op.execute(r1.get(i), r2.get(i));
			bldr.add(q);
		}
		return bldr.result();
		
	}

	
	QObject evalBinaryDouble(QObject arg1, QObject arg2, final doubleBinaryOperable op)
	{
		return evalBinaryGeneric(arg1, arg2, new binaryOperable() {
			public QObject execute(QObject i, QObject j) {
				return op.execute(i.getDouble(), j.getDouble());
			}			
		});
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

	// <
	QObject evalLT(QObject arg1, QObject arg2)
	{
		return evalBinaryDouble(arg1, arg2, new doubleBinaryOperable() {
			public QObject execute(double i, double j) {
				return QObject.createLogical(i < j);
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
	QObject evalGE(QObject arg1, QObject arg2)
	{
		return evalBinaryDouble(arg1, arg2, new doubleBinaryOperable() {
			public QObject execute(double i, double j) {
				return QObject.createLogical(i >= j);
			}
			
		});
	}
	public QObject evalEQ(QObject arg1, QObject arg2) {
		return evalBinaryDouble(arg1, arg2, new doubleBinaryOperable() {
			public QObject execute(double i, double j) {
				return QObject.createLogical(i == j);
			}
			
		});
	}
	public QObject evalNE(QObject arg1, QObject arg2) {
		return evalBinaryDouble(arg1, arg2, new doubleBinaryOperable() {
			public QObject execute(double i, double j) {
				return QObject.createLogical(i != j);
			}
		});
	}
	public QObject evalAND(QObject arg1, QObject arg2) {
		return evalBinaryGeneric(arg1, arg2, new binaryOperable() {
			public QObject execute(QObject i, QObject j) {
				return QObject.createLogical(i.isTrue() && j.isTrue());
			}
		});
	}
	
	public QObject evalBinary(Tree op, Tree arg1, Tree arg2) {
		if(QParser.LEFT_ASSIGN == op.getType() ||
				QParser.EQ_ASSIGN == op.getType())
		{
			Assignable assignable = evalLexpr(arg1);
			assignable.assign(evalExpr(arg2).QClone());
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
		else if(QParser.LT == op.getType())
		{
			return evalLT(term1, term2);
		}
		else if(QParser.LE == op.getType())
		{
			return evalLE(term1, term2);
		}
		else if(QParser.GT == op.getType())
		{
			return evalGT(term1, term2);
		}
		else if(QParser.GE == op.getType())
		{
			return evalGE(term1, term2);
		}
		else if(QParser.EQ == op.getType())
		{
			return evalEQ(term1, term2);
		}
		else if(QParser.NE == op.getType())
		{
			return evalNE(term1, term2);
		}
		else if(QParser.AND == op.getType())
		{
			return evalAND(term1, term2);
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
		ForestIterater<Tree> iter = createIterater(_lastTree);
		while(iter.hasNext())
		{
			ForestNode<Tree> node = iter.next();
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
