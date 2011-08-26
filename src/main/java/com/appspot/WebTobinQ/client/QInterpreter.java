package com.appspot.WebTobinQ.client;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.Tree;

public class QInterpreter {
	
	Writable _console;	
	
	public QInterpreter(Writable console) {
		this._console = console;
	}
	
	private String ensureLastEol(String codes)
	{
		if(codes.endsWith("\n"))
			return codes;
		return codes + "\n";
	}

	public void eval(String codestext)
	{
		CharStream codes = new ANTLRStringStream(ensureLastEol(codestext));
		QLexer lex = new QLexer(codes);
		CommonTokenStream tokens = new CommonTokenStream(lex);
		QParser parser = new QParser(tokens);
		try
		{
		 	QParser.prog_return r = parser.prog();
			
			_console.write("tree="+((Tree)r.tree).toStringTree());
		}
		catch(org.antlr.runtime.RecognitionException e)
		{}
	}
}
