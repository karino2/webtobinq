package com.appspot.WebTobinQ.client;

import org.antlr.runtime.tree.Tree;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.googlecode.gchart.client.GChart;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class WebTobinQ implements EntryPoint, Plotable, JSONPTableRetriever.ResumeListener {

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
  

  VerticalPanel _chartHolder = null;
  

	private void initDialog() {
		_dialogBox = new DialogBox();
		_dialogBox.setModal(false);
		_dialogBox.setText("Chart");
		_dialogBox.setAnimationEnabled(true);
		final Button closeButton = new Button("Close");
		// We can set the id of a widget by accessing its Element
		closeButton.getElement().setId("closeButton");
		VerticalPanel dialogVPanel = new VerticalPanel();
		_chartHolder = new VerticalPanel();
		_chartHolder.add(getChart());
		dialogVPanel.add(_chartHolder);
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


	QInterpreter _interpreter;
	TextAreaConsole	_console;
    public void onModuleLoad() {
	  final TextArea inputArea = new TextArea();
	  final TextArea consoleArea = new TextArea();
	  inputArea.setSize("800px", "200px");
	  consoleArea.setSize("800px", "300px");
	  

	  _console = new TextAreaConsole(consoleArea);
	  _interpreter = new QInterpreter(_console, this, new JSONPTableRetriever(this));
	  _interpreter.loadStaticScripts();

	  // use keyup because some eval (like plot) loose focus and fail to invoke default event.
	  inputArea.addKeyUpHandler(new KeyUpHandler(){
		  final int ENTER = 13;

			public void onKeyUp(KeyUpEvent event) {
				// Chrome assign C-j as downloads!
				// if(event.isControlKeyDown() && event.getNativeKeyCode() == 'J')
				if(event.getNativeKeyCode() == ENTER)
				{
					int pos = inputArea.getCursorPos();
					String codes = inputArea.getText();
					String currentLine = getCurrentLine(pos-1, codes);
					eval(currentLine);
				}				
			}
		  
		  });

      final Button evalButton = new Button("Eval All", new ClickHandler(){
		public void onClick(ClickEvent event) {
			String codes = inputArea.getText();
			eval(codes);
		}
		});
      final Button evalRegionButton = new Button("Eval Region", new ClickHandler() {
		public void onClick(ClickEvent event) {
			String codes = inputArea.getSelectedText();
			eval(codes);
		}      
      });
      final Button clearButton = new Button("Clear Console", new ClickHandler(){

		public void onClick(ClickEvent event) {
			_console.clear();			
		}});

    evalButton.addStyleName("evalButton");
    evalRegionButton.addStyleName("evalButton");
    clearButton.addStyleName("clearButton");
    
    

    RootPanel.get("inputAreaContainer").add(inputArea);
    RootPanel.get("evalButtonContainer").add(evalButton);
    RootPanel.get("clearButtonContainer").add(clearButton);
    RootPanel.get("evalRegionButtonContainer").add(evalRegionButton);
    RootPanel.get("consoleAreaContainer").add(consoleArea);

    inputArea.setFocus(true);

  
  }
    
  Tree _suspendedValue;
  public void eval(String codes) {
	try{		
		_interpreter.eval(codes);
	}
	catch(BlockException be)
	{
		_suspendedValue = be._currentValueNode;
	}
	catch(RuntimeException e)
	{
		_interpreter.println("error: " + e.toString());
		e.printStackTrace();
	}
  }
	public static String getCurrentLine(int pos, String codes) {
		int end = codes.indexOf("\n", pos);
		if (end == -1)
			end = codes.length();
		int reverseSearchOrigin = pos-1;
		if(reverseSearchOrigin == -1)
			reverseSearchOrigin = 0;
		int beg = codes.lastIndexOf("\n", reverseSearchOrigin);
		if (beg == -1)
			beg = 0;
		else
			beg += 1;
		return codes.substring(beg, end);
	}
	
	public void notifyStatus(String message) {
		_interpreter.debugPrint("<com>" + message);
	}

	public void onResume() {
		try{
			_interpreter.continueEval(_suspendedValue);
		}
		catch(BlockException be)
		{
			_suspendedValue = be._currentValueNode;
		}
		catch(RuntimeException e)
		{
			_interpreter.println("error: " + e.toString());
			e.printStackTrace();
		}
	}

	public void onResumeFail(String message) {
		_interpreter.println("JSONP error:" + message);
	}

	public void resetChart() {
		GChart oldChart = _chart;
		_chart = new GChart();
		if(_chartHolder == null)
			return;
		_chartHolder.remove(oldChart);
		_chartHolder.add(_chart);
	}
}
