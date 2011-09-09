package com.appspot.WebTobinQ.client;

import java.util.ArrayList;

import com.google.gwt.http.client.URL;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class JSONPTableRetriever implements TableRetrievable {
	public interface ResumeListener {
		void onResume();
		void onResumeFail(String message);
		void notifyStatus(String message);
	}

	ResumeListener _listener;
	
	public JSONPTableRetriever(ResumeListener listener) {
		_listener = listener;
	}
	
	JSONTable _table;
	
	String _prevTableName = null;
	String _prevUrl = null;
	ArrayList<String> _prevFields = null;
	RetrieveArgument _prevArg = null;
	
	public void onDataComming(JSONTable table) {
		_table = table;
	}

	public JSONTable retrieve(String url, String tableName,
			ArrayList<String> fields, RetrieveArgument arg) throws BlockException {
		if(sameAsPrevRequest(url, tableName, fields, arg) && _table != null) 
			return _table;
		
		_table = null;
		String builtUrl = buildURL(url, tableName, fields.toArray(new String[]{}), arg);
		
		_prevUrl = url;
		_prevTableName = tableName;
		_prevFields = fields;
		_prevArg = arg;
		
		 JsonpRequestBuilder requestBuilder = new JsonpRequestBuilder();
		 requestBuilder.setTimeout(10000);
		 _listener.notifyStatus("begin request...");
		 requestBuilder.requestObject(builtUrl, new AsyncCallback<JSONTable.JSONNativeTable>() {

			public void onFailure(Throwable caught) {
				 _listener.notifyStatus("request failure.");				
				_listener.onResumeFail(caught.toString());
			}

			public void onSuccess(JSONTable.JSONNativeTable result) {
				 _listener.notifyStatus("request success.");
				_table = new JSONTable(result);
				_listener.onResume();
			}
		 });

		throw new BlockException();
	}
	
	public String URLEncode(String encodee)
	{
		return URL.encode(encodee);
	}

	public String buildURL(String url, String tableName, String[] fields,
			RetrieveArgument arg) {
		StringBuilder bldr = new StringBuilder();
		bldr.append(url);
		bldr.append(URLEncode(tableName));
		bldr.append("/json?");
		if(fields.length > 0){
			bldr.append("f=");
			for(int i = 0; i < fields.length; i++)
			{
				if(i != 0)
					bldr.append(",");
				bldr.append(URLEncode(fields[i]));
			}
			bldr.append("&");
		}
		bldr.append("n=");
		bldr.append(arg._num);
		if(arg._begin != null)
		{
			bldr.append("&r=");
			bldr.append(URLEncode(String.valueOf(arg._begin)));
			bldr.append(",");
			bldr.append(URLEncode(String.valueOf(arg._end)));
		}
		return bldr.toString();
	}

	private boolean sameAsPrevRequest(String url, String tableName, ArrayList<String> fields,
			RetrieveArgument arg) {
		boolean urlEq = url.equals(_prevUrl);
		boolean tableNameEq = tableName.equals(_prevTableName);
		boolean fieldEq = fields == null && _prevFields == null || (fields != null && fields.equals(_prevFields));
		boolean argEq = arg.equals(_prevArg);
		return urlEq && tableNameEq && fieldEq && argEq;
	}

}
