package com.appspot.WebTobinQ.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.googlecode.gchart.client.GChart;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class WebTobinQ implements EntryPoint {

  public void onModuleLoad() {
	  /*
	  // begin antlr test
	CharStream input= new ANTLRStringStream("x <- c(2,3,7,9)\n");
	QLexer lex = new QLexer(input);
	CommonTokenStream tokens = new CommonTokenStream(lex);
	QParser parser = new QParser(tokens);
	try
	{
	 	QParser.prog_return r = parser.prog();
		
		parseResult = "tree="+((Tree)r.tree).toStringTree();
	}
	catch(org.antlr.runtime.RecognitionException e)
	{}
    final Label resultLabel = new Label(parseResult);
    RootPanel.get("resultLabelContainer").add(resultLabel);
	    // end antlr test
	*/

	  final TextArea inputArea = new TextArea();
	  final TextArea consoleArea = new TextArea();
	  inputArea.setSize("800px", "150px");
	  consoleArea.setSize("800px", "150px");

	  final QInterpreter interpreter = new QInterpreter(new TextAreaConsole(consoleArea));

    final Button evalButton = new Button("Eval All", new ClickHandler(){
		public void onClick(ClickEvent event) {
			interpreter.eval(inputArea.getText());
		}});
    final Button clearButton = new Button("Clear Console", new ClickHandler(){

		public void onClick(ClickEvent event) {
			// tmp. chart test code.
		    // begin GChart test
		    GChart chart = new GChart();
		    chart.setChartTitle("<b>x<sup>2</sup> vs x</b>");
		    chart.setChartSize(150, 150);
		    chart.addCurve();
		    for (int i = 0; i < 10; i++) 
		    	chart.getCurve().addPoint(i,i*i);
		    chart.getCurve().setLegendLabel("x<sup>2</sup>");
		    chart.getXAxis().setAxisLabel("x");
		    chart.getYAxis().setAxisLabel("x<sup>2</sup>");
		    RootPanel.get("chartContainer").add(chart);
		    chart.update();
		    // end GChart test		
		}});

    evalButton.addStyleName("evalButton");
    clearButton.addStyleName("clearButton");
    
    

    RootPanel.get("inputAreaContainer").add(inputArea);
    RootPanel.get("evalButtonContainer").add(evalButton);
    RootPanel.get("clearButtonContainer").add(clearButton);
    RootPanel.get("consoleAreaContainer").add(consoleArea);

    inputArea.setFocus(true);

  
  }
}
