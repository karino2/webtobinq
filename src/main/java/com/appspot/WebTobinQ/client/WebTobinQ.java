package com.appspot.WebTobinQ.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.googlecode.gchart.client.GChart;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class WebTobinQ implements EntryPoint, Plotable {

  DialogBox _dialogBox;
	
  GChart _chart;
  /* (non-Javadoc)
 * @see com.appspot.WebTobinQ.client.Plotable#getChart()
 */
  public GChart getChart()
  {
	  if(_chart == null)
		  _chart = new GChart();
	  return _chart;
  }
  
  /* (non-Javadoc)
 * @see com.appspot.WebTobinQ.client.Plotable#showChart()
 */
  public void showChart()
  {
	  // Create the popup dialog box
	  if(_dialogBox == null){
		  initDialog();
	  }
	  _chart.update();
	  _dialogBox.show();
  }

	private void initDialog() {
		_dialogBox = new DialogBox();
		_dialogBox.setText("Chart");
		_dialogBox.setAnimationEnabled(true);
		final Button closeButton = new Button("Close");
		// We can set the id of a widget by accessing its Element
		closeButton.getElement().setId("closeButton");
		VerticalPanel dialogVPanel = new VerticalPanel();
		dialogVPanel.add(getChart());
		dialogVPanel.addStyleName("dialogVPanel");
		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);
		dialogVPanel.add(closeButton);
		_dialogBox.setWidget(dialogVPanel);
	  
		// Add a handler to close the DialogBox
		closeButton.addClickHandler(new ClickHandler() {
		  public void onClick(ClickEvent event) {
		    _dialogBox.hide();
		  }
		});
	}


	TextAreaConsole	_console;
    public void onModuleLoad() {
	  final TextArea inputArea = new TextArea();
	  final TextArea consoleArea = new TextArea();
	  inputArea.setSize("800px", "150px");
	  consoleArea.setSize("800px", "150px");

	  _console = new TextAreaConsole(consoleArea);
	  final QInterpreter interpreter = new QInterpreter(_console, this);

      final Button evalButton = new Button("Eval All", new ClickHandler(){
		public void onClick(ClickEvent event) {
			try{
				interpreter.eval(inputArea.getText());
			}
			catch(RuntimeException e)
			{
				interpreter.println("error: " + e.toString());
			}
		}});
      final Button clearButton = new Button("Clear Console", new ClickHandler(){

		public void onClick(ClickEvent event) {
			_console.clear();
			// tmp. chart test code.
		    // begin GChart test
			/*
			 * 
		    GChart chart = new GChart();
		    chart.setChartTitle("<b>x<sup>2</sup> vs x</b>");
		    chart.setChartSize(150, 150);
		    chart.addCurve();
		    for (int i = 0; i < 10; i++) 
		    	chart.getCurve().addPoint(i,i*i);
		    chart.getCurve().setLegendLabel("x<sup>2</sup>");
		    chart.getXAxis().setAxisLabel("x");
		    chart.getYAxis().setAxisLabel("x<sup>2</sup>");
			
			
		    // Create the popup dialog box
			if(_dialogBox == null)
				_dialogBox = new DialogBox();
		    _dialogBox.setText("Chart");
		    _dialogBox.setAnimationEnabled(true);
		    final Button closeButton = new Button("Close");
		    // We can set the id of a widget by accessing its Element
		    closeButton.getElement().setId("closeButton");
		    VerticalPanel dialogVPanel = new VerticalPanel();
		    dialogVPanel.add(chart);
		    dialogVPanel.addStyleName("dialogVPanel");
		    dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		    dialogVPanel.add(closeButton);
		    _dialogBox.setWidget(dialogVPanel);
		    chart.update();
	        _dialogBox.show();

		    // Add a handler to close the DialogBox
		    closeButton.addClickHandler(new ClickHandler() {
		      public void onClick(ClickEvent event) {
		        _dialogBox.hide();
		      }
		    });
		    */
			
			//
			
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
