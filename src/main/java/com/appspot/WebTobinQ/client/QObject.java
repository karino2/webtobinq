package com.appspot.WebTobinQ.client;

import java.util.ArrayList;
import java.util.HashMap;

public class QObject {
	// typename
	public static final String CHARACTER_TYPE = "character";
	
	
	public static final QObject NA = new QObject("logical");
	public static final QObject Null = new QObject("NULL");
	public static QObject TRUE = new QObject("logical", 1);
	public static QObject FALSE = new QObject("logical", 0);

	
	Object _val;
	String _mode;
	HashMap<String, QObject> _attributes;
	ArrayList<QObject> _vector = null;
	
	public QObject(String mode, Object val, HashMap<String, QObject> attrs)
	{
		this(mode, val);
		if(attrs != null)
		{
			copyAttributes(attrs);
		}
	}

	private void copyAttributes(HashMap<String, QObject> attrs) {
		_attributes = new HashMap<String, QObject>();
		_attributes.putAll(attrs);
	}
	
	public QObject(String mode, Object val)
	{
		this(mode);
		_val = val;
	}
	
	public QObject(String mode)
	{
		_mode = mode;
	}
	
	public static QObject createInt(int val)
	{
		return new QObject("integer", val);
	}
	
	public static QObject createNumeric(double val)
	{
		return new QObject("numeric", val);
	}

	public static QObject createCharacter(String val)
	{
		return new QObject(CHARACTER_TYPE, val);
	}
	
	// args must be list of vector.
	public static QObject createDataFrameFromVector(QObject args)
	{
		QObject ret = createDataFrame();
		
		QObject rowNames = rowNames(args);
		ret.setAttribute("row.names", rowNames);		
		
		QObjectBuilder nameBldr = new QObjectBuilder();
		for(int i = 0; i < args.getLength(); i++)
		{
			QObject o = args.get(i);
			QObject df = copyAsDataFrame(o);
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

	private static QObject rowNames(QObject args) {
		QObjectBuilder rowBuilder = new QObjectBuilder();
		QObject o2 = args.get(0);
		for(int j = 0; j < o2.getLength(); j++)
		{
			rowBuilder.add(QObject.createCharacter(String.valueOf(j+1)));
		}
		QObject rowNames = rowBuilder.result();
		return rowNames;
	}
	
	private static QObject copyAsDataFrame(QObject o) {
		QObject df = QObject.createDataFrame();
		for(int i = 0; i < o.getLength(); i++)
			df.set(i, o.get(i).QClone());
		return df;
	}

	public QObject QClone() {
		if(getMode() == "list")
		{
			QObject ret = new QObject(getMode(), _val, _attributes);
			for(int i = 0; i < getLength(); i++)
			{
				ret.set(i, get(i).QClone());
			}
			return ret;
		}
		else  if(getLength() == 1){
			return new QObject(getMode(), _val, _attributes);
		}
		else
		{
			QObjectBuilder bldr = new QObjectBuilder();
			for(int j = 0; j < getLength(); j++)
			{
				bldr.add(get(j).QClone());
			}
			QObject ret2 =  bldr.result();
			ret2.copyAttributes(_attributes); //get(0).QClone() does not copy attributes.
			return ret2;
		}
	}

	public static QObject createDataFrame()
	{
		QObject df = new QObject("list");
		df.setAttribute("class", createCharacter("data.frame"));
		return df;
	}
	
	public String getMode()
	{
		return _mode;
	}
	
	// getClass is used in Java Object!
	public String getQClass()
	{
		if(_attributes == null ||
				!_attributes.containsKey("class"))
			return "";
		return (String)_attributes.get("class")._val;
	}
	
	public void setAttribute(String name, QObject val)
	{
		if(_attributes == null)
			_attributes = new HashMap<String, QObject>();
		_attributes.put(name, val);
	}
	
	public QObject getAttribute(String name)
	{
		if(_attributes == null ||
				!_attributes.containsKey(name))
			return QObject.Null;
		return _attributes.get(name);
	}
	
	public Object getValue()
	{
		return _val;
	}

	public String toStringOne(QObject obj)
	{
		if(obj.getMode() == "logical")
		{
			if(obj == QObject.NA)
				return "NA";
			else if((Integer)obj._val == 1)
				return "TRUE";
			else
				return "FALSE";
		}
		else if(obj.getMode() == "list")
		{
			// not R compatible.  
			return "list: " + obj.toString();
		}
		else
		{
			if(obj.getMode() == "NULL")
				return "NULL";
			return obj._val.toString();
		}
	}
	
	public boolean isDataFrame()
	{
		return getMode() == "list" && getQClass() == "data.frame";
	}
	
	public String toString()
	{
		if(isDataFrame())
			return toStringDataFrame();
		if(_vector == null)
			return toStringOne(this);
		StringBuffer buf = new StringBuffer();
		for(QObject obj : _vector)
		{
			if(buf.length() != 0)
				buf.append(" ");
			buf.append(toStringOne(obj));
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

	public int getLength()
	{
		if(_vector == null)
			return 1;
		return _vector.size();
	}
	
	public QObject recycle(int upto)
	{
		if(upto < getLength())
			return this;
		ensureVector();
		QObject ret = shallowClone();
		int index = 0;
		for(int i = 0; i < upto; i++, index++)
		{
			if(index == getLength())
				index = 0;
			ret.set(i, get(index));
		}
		return ret;
	}
	
	public QObject get(int i)
	{
		// atom
		if(getMode() != "list" && i == 0 && getLength() == 1)
			return this;
		if(_vector == null)
			return QObject.NA;
		return _vector.get(i);
	}

	public void set(int i, QObject qObject) {
		ensureVector();
		if(getMode() == "list")
		{
			setToList(i, qObject.QClone());
			return;
		}
		
		if(getLength() < i+1)
		{
			extendVectorAndFillNA(i+1);
		}
		if(i == 0) {
			// something strange.
			_val = qObject._val;
			_vector.set(i, qObject.QClone());
		}
		else
			_vector.set(i, qObject.QClone());		
	}

	private void setToList(int i, QObject qObject) {
		if(getLength() < i+1)
			extendVectorAndFillValue(i+1, QObject.Null);
		_vector.set(i, qObject);
	}

	public void extendVectorAndFillNA(int upto) {
		extendVectorAndFillValue(upto, NA);
	}
	
	// slow.
	public void extendVectorAndFillValue(int upto, QObject obj)
	{
		for(int i = _vector.size(); i < upto; i++)
		{
			_vector.add(i, obj);
		}		
	}

	void ensureVector() {
		if(_vector == null)
		{
			_vector = new ArrayList<QObject>();
			if(getMode() == "list")
				return;
			// Here, _vector.sie() == 0 but _val != 0 the case. QClone doesn't work.
			_vector.add(0, shallowClone()); 		
		}
	}
	
	public QObject shallowClone()
	{
		return new QObject(getMode(), _val, _attributes);
	}

	// R equals is binary operater who handles vector.
	// this is Java equals, it might be confusing. and almost only for test.
	public boolean equals(Object arg)
	{
		QObject robj = (QObject)arg;

		if(robj == null)
			return false;
		if(robj.getLength() != getLength())
			return false;
		if(getLength() == 1)
		{
			return equalOne(this, robj);
		}
		for(int i = 0; i < robj.getLength(); i++)
		{
			// treat NA as false.
			if(!equalOne(get(i), robj.get(i)))
				return false;
		}
		return true;
	}
	
	public int hashCode(){
		int hash = 0;
		if(getLength() == 1)
			hash = _val.hashCode();
		else
		{
			for(int i = 0; i < getLength(); i++)
				hash += get(i).hashCode();
		}
		return getMode().hashCode()+hash;
	}

	private boolean equalOne(QObject l, QObject r) {
		if(l.getMode() != r.getMode())
			return false;
		if(l.getValue() == r.getValue())
			return true; // include NULL.
		return (l.getValue().equals(r.getValue()));
	}

	public static QObject createLogical(boolean b) {
		if(b)
			return TRUE.QClone();
		return FALSE.QClone();
	}

	public static QObject createList() {
		return new QObject("list");
	}
}
