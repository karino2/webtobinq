package com.appspot.WebTobinQ.client;

import java.util.ArrayList;
import java.util.HashMap;

public class QList extends QObject {

	public QList() {
		this(null);
	}
	public QList(HashMap<String, QObject> attrs) {
		super("list", null, attrs);
		_vector = new ArrayList<QObject>();
	}
	
	public void set(int i, QObject qObject) {
		if(getLength() < i+1)
			extendVectorAndFillValue(i+1, QObject.Null);
		_vector.set(i, qObject.QClone());
	}
	
	public QObject get(int i)
	{
		if(_vector.size() > i)
			return _vector.get(i);
		return QObject.NA;
	}
	
	public QObject QClone() {
		QObject ret = new QList(_attributes);
		for(int i = 0; i < getLength(); i++)
		{
			ret.set(i, get(i).QClone());
		}
		return ret;
	}
	public static QObject createDataFrame()
	{
		QList df = new QList();
		df.setAttribute("class", QObject.createCharacter("data.frame"));
		return df;
	}
	
	public boolean isDataFrame()
	{
		return getQClass() == "data.frame";
	}
	
	public String toString()
	{
		if(isDataFrame())
			return toStringDataFrame();
		
		// nest of list does not handle properly.
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < getLength(); i++)
		{
			buf.append("[[" + i + "]]\n");
			buf.append("[1] ");
			buf.append(get(i).toString());
		}
		return buf.toString();
	}
	// slow...
	private String toStringDataFrame() {
		QObject rowNames = getAttribute("row.names");
		QObject names = getAttribute("names");
		
		ArrayList<Integer> colMaxLength = new ArrayList<Integer>();
		colMaxLength.add(0,maxStrLength(rowNames));
		StringBuffer buf = new StringBuffer();
	
		// print header
		appendSpace(buf, colMaxLength.get(0));
		for(int i = 0; i < names.getLength(); i++)
		{
			buf.append(" ");
			
			QObject name = names.get(i);
			int nameLen = name.toString().length();
			colMaxLength.add(i+1, Math.max(nameLen, maxStrLength(get(i))));
			buf.append(name.toString());
			appendSpace(buf, colMaxLength.get(i+1) - nameLen);
		}
		buf.append("\n");
		
		QObject firstList = get(0);
		for(int i = 0; i < firstList.getLength(); i++)
		{
			String rowName = rowNames.get(i).toString();
			buf.append(rowName);
			appendSpace(buf, colMaxLength.get(0) - rowName.length());
			for(int j = 0; j < getLength(); j++) {
				buf.append(" ");
				String val = get(j).get(i).toString();
				buf.append(val);
				appendSpace(buf, colMaxLength.get(i+1) - val.length());
			}
			buf.append("\n");
		}
		return buf.toString();
	}

	private void appendSpace(StringBuffer buf, int maxRowLength) {
		for(int i = 0; i < maxRowLength; i++)
			buf.append(" ");
	}

	private int maxStrLength(QObject rowNames) {
		int max = 0;
		for(int i = 0; i < rowNames.getLength(); i++)
		{
			int l = rowNames.get(i).toString().length();
			if(max < l)
				max = l;
		}
		return max;
	}
	protected static QObject copyAsDataFrame(QObject o) {
		QObject df = createDataFrame();
		for(int i = 0; i < o.getLength(); i++)
			df.set(i, o.get(i).QClone());
		return df;
	}
	protected static QObject rowNames(QObject args) {
		QObjectBuilder rowBuilder = new QObjectBuilder();
		QObject o2 = args.get(0);
		for(int j = 0; j < o2.getLength(); j++)
		{
			rowBuilder.add(QObject.createCharacter(String.valueOf(j+1)));
		}
		QObject rowNames = rowBuilder.result();
		return rowNames;
	}
	// args must be list of vector.
	public static QObject createDataFrameFromVector(QObject args)
	{
		QObject ret = createDataFrame();
		
		QObject rowNames = QList.rowNames(args);
		ret.setAttribute("row.names", rowNames);		
		
		QObjectBuilder nameBldr = new QObjectBuilder();
		for(int i = 0; i < args.getLength(); i++)
		{
			QObject o = args.get(i);
			QObject df = QList.copyAsDataFrame(o);
			ret.set(i, df);
	
			QObject name = null;
			if(QObject.Null.equals(o.getAttribute("names")))
				name = QObject.createCharacter("V" + (i+1));
			else
				name = o.getAttribute("names");
	
			nameBldr.add(name);
			df.setAttribute("names", name);
			df.setAttribute("row.names", rowNames);
		}
		ret.setAttribute("names", nameBldr.result());
		return ret;
	}
	public static QObject createList() {
		return new QList();
	}
}
