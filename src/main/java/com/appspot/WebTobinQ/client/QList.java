package com.appspot.WebTobinQ.client;

import java.util.ArrayList;
import java.util.HashMap;

public class QList extends QObject {
	public static final String LIST_TYPE = "list";
	public static final String DATAFRAME_CLASS = "data.frame";

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
		_vector.set(i, qObject);
	}
	
	public QObject getBBInt(int i)
	{
		if(isDataFrame())
		{
			return get(i).get(0);
		}
		return get(i); // currently, get of list return contents.
	}
	
	// getBB return the contents of row list. not row list itself.
	public QObject getBB(QObject arg)
	{
		if(arg.isNumber())
			return getBBInt(arg.getInt()-1);
		if(arg.getMode() != CHARACTER_TYPE)
			throw new RuntimeException("Arg of [[]] neither number nor string: " + arg.getMode());
		String colName = (String)arg.getValue();
		int i = getIndex(colName);
		return getBBInt(i);
	}
	
	private int getIndex(String colName) {
		QObject names = getNamesAttr();
		for(int i = 0; i < names.getLength(); i++)
		{
			QObject name = names.get(i);
			if(name.getValue().equals(colName))
				return i;
		}
		throw new RuntimeException("Arg of [[]] does not match to names: " + colName);
	}
	
	private QObject getName(int colIndex) {
		return getNamesAttr().get(colIndex);
	}
	
	private QObject getNamesAttr() {
		return getAttribute("names");
	}
	private void setNamesAttr(QObject namesObj)
	{
		setAttribute("names", namesObj);		
	}
	private QObject getRowNamesAttr() {
		QObject rowNames = getAttribute("row.names");
		return rowNames;
	}
	private void setRowNamesAttr(QObject rowNames) {
		setAttribute("row.names", rowNames);
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
	
	public String getNameOfIndex(int i)
	{
		QObject names = getNamesAttr();
		if(names.isNull())
			return "[[" + String.valueOf(i+1) + "]]";
		return "$" + (String)names.get(i).getValue();
	}
	
	private String toStringList() {
		// nest of list does not handle properly.
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < getLength(); i++)
		{
			buf.append(getNameOfIndex(i));
			buf.append("\n");
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
		df.setAttribute("class", QObject.createCharacter(DATAFRAME_CLASS));
		return df;
	}
	
	public boolean isDataFrame()
	{
		return getQClass() == "data.frame";
	}
	
	
	// slow...
	private String toStringDataFrame() {
		QObject rowNames = getRowNamesAttr();
		QObject names = getNamesAttr();
		
		ArrayList<Integer> colMaxLength = new ArrayList<Integer>();
		colMaxLength.add(0,maxRawStrLength(rowNames));
		StringBuffer buf = new StringBuffer();
	
		// print header
		appendSpace(buf, colMaxLength.get(0));
		for(int i = 0; i < names.getLength(); i++)
		{
			buf.append(" ");
			
			QObject name = names.get(i);
			int nameLen = name.toRawString().length();
			colMaxLength.add(i+1, Math.max(nameLen, maxRawStrLength(get(i).get(0))));
			buf.append(name.toRawString());
			appendSpace(buf, colMaxLength.get(i+1) - nameLen);
		}
		buf.append("\n");
		
		QObject firstList = get(0);
		QObject firstVector = firstList.get(0);
		for(int i = 0; i < firstVector.getLength(); i++)
		{
			String rowName = rowNames.get(i).toRawString();
			buf.append(rowName);
			appendSpace(buf, colMaxLength.get(0) - rowName.length());
			for(int j = 0; j < getLength(); j++) {
				buf.append(" ");
				String val = rawGetByRowCol(i, j).toString();
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

	private int maxRawStrLength(QObject rowNames) {
		int max = 0;
		for(int i = 0; i < rowNames.getLength(); i++)
		{
			int l = rowNames.get(i).toRawString().length();
			if(max < l)
				max = l;
		}
		return max;
	}
	
	public static QList createDataFrameFromJSONTable(JSONTable table) {
		QList ret = createDataFrame();
		QObject rowNames = defaultRowNames(table.getRowNum());
		ret.setRowNamesAttr(rowNames);
		
		ArrayList<QList> cols = new ArrayList<QList>();
		QObjectBuilder nameBldr = new QObjectBuilder();
		
		setupColsNames(table, rowNames, cols, nameBldr);
		ret.setNamesAttr(nameBldr.result());
		
		copyDatas(table, cols);
		
		for(int k = 0; k < table.getColumnNum(); k++)
		{
			ret.set(k, cols.get(k));
		}
		return ret;
	}
	
	
	
	private static void copyDatas(JSONTable table, ArrayList<QList> cols) {
		for(int row = 0; row < table.getRowNum(); row++)
		{
			for(int col = 0; col < table.getColumnNum(); col++)
			{
				if(table.isNA(row, col))
					cols.get(col).rawSetByRowCol(row, 0, QObject.NA);
				else
					cols.get(col).rawSetByRowCol(row, 0, QObject.createNumeric(table.getItemNumeric(row, col)));
			}
		}
	}
	private static void setupColsNames(JSONTable table, QObject rowNames,
			ArrayList<QList> cols, QObjectBuilder nameBldr) {
		for(int i = 0; i < table.getColumnNum(); i++)
		{
			QList col = createDataFrame();
			col.set(0, QObject.createNumeric(0));
			
			QObject name = null;
			name = QObject.createCharacter(table.getTitle(i));
	
			nameBldr.add(name);
			col.setNamesAttr(name);
			col.setRowNamesAttr(rowNames);
			cols.add(col);
		}
	}
	protected static QList copyVectorAsDataFrame(QObject o) {
		QList df = createDataFrame();
		QObjectBuilder bldr = new QObjectBuilder();
		for(int i = 0; i < o.getLength(); i++)
			bldr.add(o.get(i).QClone()); //TODO: remove clone.
		df.set(0, bldr.result());
		return df;
	}
	
	protected static QObject rowNames(QObject args) {
		QObject o2 = args.get(0);
		int rowNum = o2.getLength();
		return defaultRowNames(rowNum);
	}
	
	static QObject defaultRowNames(int rowNum) {
		QObjectBuilder rowBuilder = new QObjectBuilder();
		for(int j = 0; j < rowNum; j++)
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
		ret.setRowNamesAttr(rowNames);		
		
		QObjectBuilder nameBldr = new QObjectBuilder();
		for(int i = 0; i < args.getLength(); i++)
		{
			QObject o = args.get(i);
			QList df = QList.copyVectorAsDataFrame(o);
	
			QObject name = null;
			if(QObject.Null.equals(o.getAttribute("names")))
				name = QObject.createCharacter("V" + (i+1));
			else
				name = o.getAttribute("names");
	
			nameBldr.add(name);
			df.setNamesAttr(name);
			df.setRowNamesAttr(rowNames);
			// inside set, df is copied. so you must call here.
			ret.set(i, df);
		}
		ret.setNamesAttr(nameBldr.result());
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

	public QList dupBaseDataFrame() {
		QList df = QList.createDataFrame();
		df.setNamesAttr(getNamesAttr().QClone());
		for(int col = 0; col < getLength(); col++) {
			df.addDataFrameColumn(col);
		}
		return df;
	}
	
	// get is too generic and hard to understand code.
	public QList getColumn(int colIndex) {
		return (QList)get(colIndex);
	}
	
	public void setRowName(int rowIndex, QObject name)
	{
		getRowNamesAttr().set(rowIndex, name);
	}
	
	public QObject getRowName(int rowIndex)
	{
		return getRowNamesAttr().get(rowIndex);
	}
	
	public QObject subscriptByRowIndex(int rowIndex) {
		QList df = dupBaseDataFrame();
		df.setRowName(0, getRowName(rowIndex).QClone());
		
		for(int col = 0; col < getLength(); col++) {
			QObject colVector = getBBInt(col);
			df.getColumn(col).set(0, colVector.get(rowIndex));
		}
		return df;
	}

	private QList addDataFrameColumn(int colIndex) {
		QList col = QList.createDataFrame();
		col.setNamesAttr(getName(colIndex));
		set(colIndex, col);
		return col;
	}
	public QObject subscriptByCol(int i) {
		return getBBInt(i);
	}
	
	QObject rawGetByRowCol(int row, int col)
	{
		return get(col).get(0).get(row);
	}
	boolean isPartialDataFrame()
	{
		return (getLength() == 0 ||
				getLength() == 1);
	}
	void rawSetByRowCol(int row, int col, QObject obj)
	{
		// column of data.frame is also data.frame.
		// So subscription never reached to contents.
		// But as a implementation point of view, some kind of bootstrap is necessary.
		// I use partial data.frame for column, that is, contents of first element is vector.
		// So here is one those kind of special handling.
		if(isPartialDataFrame() && col== 0)
		{
			if(row == 0)
				set(0, obj);
			else
				get(0).set(row, obj);
		}
		else
			get(col).get(0).set(row, obj);		
	}
	
	QObject subscriptByRow(QObject rowRange) {
		if(rowRange.getLength () == 1)
		{
			int  index = rowRange.getInt();
			return subscriptByRowIndex(index-1);
		}
		QList df = dupBaseDataFrame();
		
		for(int newRowIndex = 0; newRowIndex < rowRange.getLength(); newRowIndex++)
		{
			int orgRowIndex = rowRange.get(newRowIndex).getInt() -1;
			df.setRowName(newRowIndex, getRowName(orgRowIndex));
			QList row = (QList)subscriptByRowIndex(orgRowIndex);
			for(int col = 0; col < getLength(); col++)
			{
				QList columnDf = df.getColumn(col);
				columnDf.setRowName(newRowIndex, getRowName(orgRowIndex));
				columnDf.rawSetByRowCol(newRowIndex, 0, row.rawGetByRowCol(0, col));
			}
		}
		return df;
	}
}
