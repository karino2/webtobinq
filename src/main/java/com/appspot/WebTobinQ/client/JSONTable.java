package com.appspot.WebTobinQ.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.core.client.JsArrayString;

public class JSONTable {
	public static class JSONNativeTable extends JavaScriptObject 
	{
		protected JSONNativeTable(){}
		public final native double convertToDouble(JavaScriptObject jso) /*-{
		   return jso;
		}-*/;
		
		public final native boolean isNA(int row, int col) /*-{
			return this.data[row][col] == "NA";
		}-*/;
	
		public final native JsArrayString getTitles() /*-{
			return this.titles;
	    }-*/;
		public final native JsArrayString getTypes() /*-{
			return this.types;
		}-*/;
		public final native JsArray<JsArray<JavaScriptObject>> getData() /*-{
			return this.data;
		}-*/;
	}
	
	JSONNativeTable _ntable;
	public JSONTable(JSONNativeTable ntable){
		_ntable = ntable;
	}
		
	public int getColumnNum(){
		return _ntable.getTitles().length();
	}
	
	public int getRowNum() {
		return _ntable.getData().length();
	}
	
	public String getTitle(int col) {
		return _ntable.getTitles().get(col);
	}
	
	public String getType(int col) {
		return _ntable.getTypes().get(col);
	}

	
	public boolean isNA(int row, int col){
		return _ntable.isNA(row, col);
	}
	public double getItemNumeric(int row, int col) {
		JsArray<JavaScriptObject> rowAr = _ntable.getData().get(row);
		double ret = ((JsArrayNumber)rowAr.cast()).get(col);
		return ret;
//		JavaScriptObject jsObj = rowAr.get(col);
//		return _ntable.convertToDouble(jsObj);
//		return _ntable.convertToDouble(_ntable.getData().get(row).get(col));
	}
	
}
