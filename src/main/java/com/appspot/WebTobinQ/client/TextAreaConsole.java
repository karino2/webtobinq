package com.appspot.WebTobinQ.client;

import com.google.gwt.user.client.ui.ValueBoxBase;

public class TextAreaConsole implements Writable {

	ValueBoxBase<String> _textArea;
	
	public TextAreaConsole(ValueBoxBase<String> tarea)
	{
		_textArea = tarea;
	}
	
	public void write(CharSequence cs)
	{
		_textArea.setText(_textArea.getText() + cs);		
	}
}
