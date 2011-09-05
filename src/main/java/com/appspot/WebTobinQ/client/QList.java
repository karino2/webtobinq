package com.appspot.WebTobinQ.client;

import java.util.ArrayList;
import java.util.HashMap;

public class QList extends QObject {
	public static final String LIST_TYPE = "list";

	public QList() {
		this(null);
	}
	public QList(HashMap<String, QObject> attrs) {
		super(LIST_TYPE, null, attrs);
		_vector = new ArrayList<QObject>();
	}
	
	public void set(int i, QObject qObject) {
		if(getLength() < i+1)
			extendVectorAndFillValue(i+1, QObject.Null);
		_vector.set(i, qObject.QClone());
	}
	
	public QObject getBBInt(int i)
	{
		if(isDataFrame())
			return get(i).get(0);
		return get(i); // currently, get of list return contents.
	}
	
	// getBB return the contents of row list. not row list itself.
	public QObject getBB(QObject arg)
	{
		if(arg.isNumber())
			return getBBInt(arg.getInt());
		if(arg.getMode() != CHARACTER_TYPE)
			throw new RuntimeException("Arg of [[]] neither number nor string: " + arg.getMode());
		String colName = (String)arg.getValue();
		int i = getIndex(colName);
		return getBBInt(i);
	}
	
	private int getIndex(String colName) {
		QObject names = getAttribute("names");
		for(int i = 0; i < names.getLength(); i++)
		{
			QObject name = names.get(i);
			if(name.getValue().equals(colName))
				return i;
		}
		throw new RuntimeException("Arg of [[]] does not match to names: " + colName);
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
	
	public static QList createList() {
		return new QList();
	}

	public String toString()
	{
		if(isDataFrame())
			return toStringDataFrame();
		
		return toStringList();
	}
	
	private String toStringList() {
		// nest of list does not handle properly.
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < getLength(); i++)
		{
			buf.append("[[" + (i+1) + "]]\n");
			buf.append("[1] ");
			buf.append(get(i).toString());
			buf.append("\n\n");
		}
		return buf.toString();
	}
	
	// -- begin data frame dependent.
	public static QList createDataFrame()
	{
		QList df = new QList();
		df.setAttribute("class", QObject.createCharacter("data.frame"));
		return df;
	}
	
	public boolean isDataFrame()
	{
		return getQClass() == "data.frame";
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
			colMaxLength.add(i+1, Math.max(nameLen, maxStrLength(get(i).get(0))));
			buf.append(name.toString());
			appendSpace(buf, colMaxLength.get(i+1) - nameLen);
		}
		buf.append("\n");
		
		QObject firstList = get(0);
		QObject firstVector = firstList.get(0);
		for(int i = 0; i < firstVector.getLength(); i++)
		{
			String rowName = rowNames.get(i).toString();
			buf.append(rowName);
			appendSpace(buf, colMaxLength.get(0) - rowName.length());
			for(int j = 0; j < getLength(); j++) {
				buf.append(" ");
				QObject dfSub = get(j);
				String val = dfSub.get(0).get(i).toString();
				buf.append(val);
				appendSpace(buf, colMaxLength.get(j+1) - val.length());
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
		QObjectBuilder bldr = new QObjectBuilder();
		for(int i = 0; i < o.getLength(); i++)
			bldr.add(o.get(i).QClone());
		df.set(0, bldr.result());
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
	public static QList createDataFrameFromVector(QObject args)
	{
		validateArg(args);
		
		QList ret = createDataFrame();
		
		QObject rowNames = rowNames(args);
		ret.setAttribute("row.names", rowNames);		
		
		QObjectBuilder nameBldr = new QObjectBuilder();
		for(int i = 0; i < args.getLength(); i++)
		{
			QObject o = args.get(i);
			QObject df = QList.copyAsDataFrame(o);
	
			QObject name = null;
			if(QObject.Null.equals(o.getAttribute("names")))
				name = QObject.createCharacter("V" + (i+1));
			else
				name = o.getAttribute("names");
	
			nameBldr.add(name);
			df.setAttribute("names", name);
			df.setAttribute("row.names", rowNames);
			// inside set, df is copied. so you must call here.
			ret.set(i, df);
		}
		ret.setAttribute("names", nameBldr.result());
		return ret;
	}
	
	static void validateArg(QObject args) {
		if(args.getMode() != LIST_TYPE)
			throw new RuntimeException("data.frame arg is not list");
		int len = args.get(0).getLength();
		for(int i = 0; i < args.getLength(); i++)
		{
			if(args.get(i).getLength() != len)
			{
				throw new RuntimeException("data.frame arg length mismatch: 0's=" + len + ", " + i + "'s=" + args.get(i).getLength());
			}
		}
	}
}
