package com.appspot.WebTobinQ.client;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;
import static com.appspot.WebTobinQ.client.QObject.createCharacter;
import static com.appspot.WebTobinQ.client.QInterpreterTest.assertQCharEquals;
import static com.appspot.WebTobinQ.client.QInterpreterTest.assertQNumericEquals;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.junit.Test;

import com.appspot.WebTobinQ.client.QFunction.QObjectForestAdapter;
import com.appspot.WebTobinQ.client.TableRetrievable.RetrieveArgument;


public class QTypesTest {
	@Test
	public void test_QInt_equals()
	{
		QObject a = QObject.createInt(5);
		QObject b = QObject.createInt(5);
		assertEquals(a, b);
		
	}
	
	@Test
	public void test_QInt_equals_multiple()
	{
		QObject r1 = QObject.createInt(1);
		QObject r2 = QObject.createInt(2);
		
		QObject v1 = new QObject("numeric");
		v1.set(0, r1);
		v1.set(1, r2);
		
		QObject v2 = new QObject("numeric");
		v2.set(0, r1);
		v2.set(1, r2);
		
		assertEquals(v1, v2);
	}
	
	@Test
	public void test_QInt_equals_onlyFirstEqual()
	{
		QObject r1 = QObject.createInt(1);
		QObject r2 = QObject.createInt(2);
		
		QObject v1 = new QObject("numeric");
		v1.set(0, r1);
		v1.set(1, r2);
				
		assertNotSame(v1, r1);
	}
	
	@Test
	public void test_QInt_notEquals()
	{
		QObject a = QObject.createInt(5);
		QObject b = QObject.createInt(6);
		assertNotSame(a, b);
	}
	
	@Test
	public void test_QInt_set()
	{
		QObject a = QObject.createInt(1);
		a.set(2, QObject.createInt(3));

		
		assertEquals(3, a.getLength());
		assertEquals(QObject.createInt(1), a.get(0));
		assertEquals(QObject.NA, a.get(1));
		assertEquals(QObject.createInt(3), a.get(2));
		
	}
	
	@Test
	public void test_QInt_recycle()
	{
		QObject r1 = QObject.createInt(1);
		QObject r2 = QObject.createInt(2);
		QObject r3 = QObject.createInt(3);
		
		QObject r = QObject.createInt(1);
		r.set(1, r2);
		r.set(2, r3);
		
		QObject recycle = r.recycle(7);
		assertEquals(r1, recycle.get(0));
		assertEquals(r2, recycle.get(1));
		assertEquals(r3, recycle.get(2));
		assertEquals(r1, recycle.get(3));
		assertEquals(r2, recycle.get(4));
		assertEquals(r3, recycle.get(5));
		assertEquals(r1, recycle.get(6));
	}
	
	@Test
	public void test_QClone_numeric() {
		QObject expected = QObject.createNumeric(1);
		QObject actual = QObject.createNumeric(1).QClone();
		assertEquals(expected, actual);
	}
	
	@Test
	public void test_QClone_vector() {
		QObject expected = createVector12("x");

		QObject actual = createVector12("x").QClone();
		assertEquals(expected, actual);
	}
	
	@Test
	public void test_QClone_vector_keepAttribute() {
		QObject actual = createVector12("x").QClone();
		assertEquals(QObject.createCharacter("x"), actual.getAttribute("names"));
	}
	
	@Test
	public void test_QClone_list() {
		QObject l = createListOfX12();
		QObject actual = l.QClone();
		
		assertEquals("list", actual.getMode());
		assertEquals(1, actual.getLength());
		assertEquals(createVector12("X"), actual.get(0));
	}
	
	@Test
	public void test_dataFrame() {
		QObject df = QList.createDataFrame();
		assertEquals("list", df.getMode());
		assertEquals("data.frame", df.getQClass());
		assertEquals(true, ((QList)df).isDataFrame());
	}
	
	@Test
	public void test_dataFrameFromVector_names() {

		QObject args = createListOfX12();
		
		QObject df = QList.createDataFrameFromVector(args);
		assertEquals(QObject.createCharacter("X"), df.getAttribute("names"));
	}

	@Test
	public void test_dataFrameFromVector_rowNames() {

		QObject args = createListOfX12();
		
		QObject df = QList.createDataFrameFromVector(args);
		assertEquals(QObject.createCharacter("1"), df.getAttribute("row.names").get(0));
		assertEquals(QObject.createCharacter("2"), df.getAttribute("row.names").get(1));
	}
	
	@Test
	public void test_list_toString()
	{
		QObject args = QList.createList();
		QObject x = createVector12("x");
		QObject y = createVector12("y");
		args.set(0, x);
		args.set(1, y);		
		assertEquals("[[1]]\n[1] 1.0 2.0\n\n[[2]]\n[1] 1.0 2.0\n\n", args.toString());
		
	}
	
	@Test
	public void test_list_toString_withNames()
	{
		QObject args = QList.createList();
		QObject x = createVector12("x");
		args.set(0, x);
		args.setAttribute("names", QObject.createCharacter("hoge"));
		assertEquals("$hoge\n[1] 1.0 2.0\n\n", args.toString());
		
	}
	
	@Test
	public void test_getInt_numeric()
	{
		int expected = 3;		
		int actual = createNumeric(3).getInt();
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void test_list_getBB_numericAccess()
	{
		QList l = createListOfX12();
		QObject xvector = l.getBB(createNumeric(1));
		assertEquals(createVector12("X"), xvector);
	}
	
	@Test(expected=RuntimeException.class)
	public void test_list_getBB_notFoundArg()
	{
		QList l = createListOfX12();
		l.getBB(createCharacter("notExist"));
	}

	
	@Test
	public void test_list_getBB_textAccess()
	{
		QObject args = QList.createList();
		QObject x = createVector12("x");
		QObject y = createVector12("y");
		y.set(2, createNumeric(3)); // y == c(1, 2, 3)
		args.set(0, x);
		args.set(1, y);
		QObject names = QObject.createCharacter("x");
		names.set(1, QObject.createCharacter("y"));
		args.setAttribute("names", names);
		
		QObject actual = args.getBB(createCharacter("y"));
		assertEquals(y, actual);
	}
	
	@Test
	public void test_dataFrameFromVector_contents_row1() {
		QObject x = createVector12("x");
		QObject y = createVector12("y");
		QList df = createDataFrame2(x, y);
		
		assertEquals(y, df.getBBInt(1));
	}

	private QList createDataFrame3(QObject x, QObject y, QObject z) {
		QObject args = createList12(x, y);		
		args.set(2, z);
		QList df = QList.createDataFrameFromVector(args);
		return df;
	}
	
	private QList createDataFrame2(QObject x, QObject y) {
		QObject args = createList12(x, y);		
		QList df = QList.createDataFrameFromVector(args);
		return df;
	}

	private QObject createList12(QObject x, QObject y) {
		QObject args = QList.createList();
		args.set(0, x);
		args.set(1, y);
		return args;
	}
	
	@Test
	public void test_dataFrame_subscriptByRow() {
		/*   x y
		 * 1 1 1
		 * 2 2 2
		 * 3 3 3
		 */
		QList df = create123x123DataFrame();
		
		// 0 indexed in Java layer. df[2,]
		QList actual = (QList)df.subscriptByRowIndex(1);
		assertEquals(2, actual.getLength());
		assertQNumericEquals(2, actual.getBBInt(0));
		assertQNumericEquals(2, actual.getBBInt(1));	
	}

	@Test
	public void test_dataFrame_asnumeric() {
		/*   x y z
		 * 1 1 2 3
		 */
		QList df = create123DataFrame();
		
		QObject actual_vect = QFunction.asNumeric(df);
		
		assertVector123(actual_vect);
	}
	
	@Test
	public void test_characterVector_asnumeric() {
		QObject charVect = QObject.createCharacter("1");
		charVect.set(1, QObject.createCharacter("2"));
		charVect.set(2, QObject.createCharacter("3"));

		QObject actual_vect = QFunction.asNumeric(charVect);
		
		assertVector123(actual_vect);
	}

	void assertVector123(QObject actual_vect) {
		assertEquals(3, actual_vect.getLength());
		assertEquals(QObject.NUMERIC_TYPE, actual_vect.getMode());
		assertQNumericEquals(1, actual_vect.get(0));
		assertQNumericEquals(2, actual_vect.get(1));	
		assertQNumericEquals(3, actual_vect.get(2));
	}
	
	private QList create123x123DataFrame() {
		QObject x = createVector123("x");
		QObject y = createVector123("y");
		QList df = createDataFrame2(x, y);
		return df;
	}
	
	private QList create123DataFrame() {
		QObject x = QObject.createNumeric(1);
		setNameAttr("x", x);
		QObject y = QObject.createNumeric(2);
		setNameAttr("y", y);
		QObject z = QObject.createNumeric(3);
		setNameAttr("z", z);
		QList df = createDataFrame3(x, y, z);
		return df;
	}
	
	@Test
	public void test_dataFrame_subscriptByCol() {
		/*   x y
		 * 1 1 1
		 * 2 2 2
		 * 3 3 3
		 */
		QList df = create123x123DataFrame();
		
		// 0 indexed in Java layer.
		QObject actual = df.subscriptByCol(1);
		assertEquals(3, actual.getLength());
		assertQNumericEquals(1, actual.get(0));
		assertQNumericEquals(2, actual.get(1));	
		assertQNumericEquals(3, actual.get(2));	
	}
	
	@Test
	public void test_dataFrameFromVector_toString() {
		QObject args = QList.createList();
		QObject x = createVector12("x");		
		QObject y = createVector12("y");		
		args.set(0, x);
		args.set(1, y);
		QObject df = QList.createDataFrameFromVector(args);
		
		assertEquals("  x   y  \n1 1.0 1.0\n2 2.0 2.0\n", df.toString());
	}
	
	@Test
	public void test_dataFrame_getBB() {
		QObject args = QList.createList();
		QObject x = createVector12("x");		
		QObject y = createVector12("y");		
		args.set(0, x);
		args.set(1, y);
		QObject df = QList.createDataFrameFromVector(args);
		
		assertEquals(y, df.getBB(createCharacter("y")));
	}
	
	@Test
	public void test_dataFrameFromVector_contents_row1Class() {
		QObject args = createListOfX12();
		
		QObject df = QList.createDataFrameFromVector(args);
		
		assertEquals("list", df.get(0).getMode());
		assertEquals("data.frame", df.get(0).getQClass());
		
	}
	private QList createListOfX12() {
		QList args = QList.createList();
		QObject x = createVector12("X");		
		args.set(0, x);
		return args;
	}

	private QObject createVector12(String name) {
		QObject x = QObject.createNumeric(1);
		x.set(1, QObject.createNumeric(2));
		setNameAttr(name, x);
		return x;
	}

	private void setNameAttr(String name, QObject obj) {
		obj.setAttribute("names", QObject.createCharacter(name));
	}
	
	private QObject createVector123(String name) {
		QObject x = createVector12(name);
		x.set(2, QObject.createNumeric(3));
		return x;
	}
	
	@Test(expected=RuntimeException.class)
	public void test_dataFrame_validateArg_lengthMismatch() {
		QObject x = createVector12("x");
		x.set(2, createNumeric(3));
		QObject y = createVector12("y");
		QList args = new QList();
		args.set(0, x);
		args.set(1, y);
		QList.validateArg(args);
	}
	
	
	class JSONTableForTest extends JSONTable
	{
		String[] _titles;
		String[] _types;
		Object[] _data;
		public JSONTableForTest(String[] titles, String[] types, Object[] data){
			super(null);
			_titles = titles;
			_types = types;
			_data = data;
		}
		
		public String getTitle(int i)
		{
			return _titles[i];
		}
		
		public int getColumnNum()
		{
			return _titles.length;
		}
		
		public boolean isNA(int row,int col)
		{
			return false;
		}
		
		public double getItemNumeric(int irow,int col)
		{
			Object[] row = (Object[])_data[irow];
			return (Double)row[col];
		}
		public int getRowNum() {
			return _data.length;
		}
	}
	
	@Test
	public void test_JSONTable()
	{
		JSONTable jt = new JSONTableForTest(new String[]{ "日付", "GDP", "消費" }, 
				new String[] {"integer", "numeric", "numeric"},
				new Object[] { new Object[]{1980.0, 312712.7, 174382.7}, new Object[]{1981.0, 321490.5, 177074.9}});
		assertEquals(3, jt.getColumnNum());
		assertEquals(2, jt.getRowNum());
		assertEquals("日付", jt.getTitle(0));
		assertEquals("GDP", jt.getTitle(1));
		assertEquals("消費", jt.getTitle(2));
		assertEquals(177074.9, jt.getItemNumeric(1, 2));
	}
	
	@Test
	public void test_JSONTable_3row()
	{
		JSONTable jt = new JSONTableForTest(new String[]{ "日付", "GDP", "消費" }, 
				new String[] {"integer", "numeric", "numeric"},
				new Object[] { new Object[]{1980.0, 312712.7, 174382.7}, new Object[]{1981.0, 321490.5, 177074.9}, new Object[]{1982.0, 331710.7 ,184799.3}});
		assertEquals(3, jt.getRowNum());
		assertEquals(184799.3, jt.getItemNumeric(2, 2));
	}
	
	@Test
	public void test_createDataFrameFromJSONTable()
	{
		JSONTable jt = new JSONTableForTest(new String[]{ "日付", "GDP", "消費" }, 
				new String[] {"integer", "numeric", "numeric"},
				new Object[] { new Object[]{1980.0, 312712.7, 174382.7}, new Object[]{1981.0, 321490.5, 177074.9}});
		QList df = QList.createDataFrameFromJSONTable(jt);
		assertEquals("  日付     GDP      消費      \n1 1980.0 312712.7 174382.7\n2 1981.0 321490.5 177074.9\n", df.toString());
	}
	
	@Test
	public void test_createDataFrameFromJSONTable_3row()
	{
		JSONTable jt = new JSONTableForTest(new String[]{ "日付", "GDP", "消費" }, 
				new String[] {"integer", "numeric", "numeric"},
				new Object[] { new Object[]{1980.0, 312712.7, 174382.7}, new Object[]{1981.0, 321490.5, 177074.9}, new Object[]{1982.0, 331710.7 ,184799.3}});
		QList df = QList.createDataFrameFromJSONTable(jt);
		assertEquals("  日付     GDP      消費      \n1 1980.0 312712.7 174382.7\n2 1981.0 321490.5 177074.9\n3 1982.0 331710.7 184799.3\n", df.toString());
	}
	
	@Test
	public void test_attributesAsList_null()
	{
		QObject i = QObject.createNumeric(0);
		QObject actual = i.attributesAsList();

		assertEquals(true, actual.isNull());
	}

	@Test
	public void test_attributesAsList_oneAttr()
	{
		QObject i = QObject.createNumeric(0);
		i.setAttribute("hoge", QObject.createCharacter("ika"));
		
		QObject actual = i.attributesAsList();
		assertEquals(1, actual.getLength());
		assertEquals("list", actual.getMode());
		assertQCharEquals("ika", actual.getBB(QObject.createCharacter("hoge")));
	}
	
	// --------- other misc test -------------
	@Test
	public void test_getCurrentLine_onlyOneLine_center()
	{
		String codes = "abc";
		int pos = 1;
		String actual = WebTobinQ.getCurrentLine(pos, codes);
		
		assertEquals(codes, actual);
	}
	
	@Test
	public void test_getCurrentLine_onlyOneLine_beg()
	{
		String codes = "abc";
		int pos = 0;
		String actual = WebTobinQ.getCurrentLine(pos, codes);
		
		assertEquals(codes, actual);
	}
	
	@Test
	public void test_getCurrentLine_onlyOneLine_end()
	{
		String codes = "abc";
		int pos = 3;
		String actual = WebTobinQ.getCurrentLine(pos, codes);
		
		assertEquals(codes, actual);
	}
	
	@Test
	public void test_getCurrentLine_threeLine_secondBegin()
	{
		String expected = "bc";
		
		String codes = "a\nbc\nde";
		int pos = 2;
		String actual = WebTobinQ.getCurrentLine(pos, codes);
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void test_getCurrentLine_threeLine_secondEnd()
	{
		String expected = "bc";
		
		String codes = "a\nbc\nde";
		int pos = 4;
		String actual = WebTobinQ.getCurrentLine(pos, codes);
		
		assertEquals(expected, actual);
	}
	
	class JSONPTableRetrieverForTest extends JSONPTableRetriever
	{
		public JSONPTableRetrieverForTest(ResumeListener listener) {
			super(listener);
		}

		public String URLEncode(String encodee)
		{
			try {
				return URLEncoder.encode(encodee, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// never reached here.
				e.printStackTrace();
				return null;
			}
		}
	}
	
	@Test
	public void test_buildURL()
	{
		String expected = "http://test/t/GDP/json?f=a1,a2&n=10&r=1990.0,2000.0";

		JSONPTableRetriever retriever = new JSONPTableRetrieverForTest(null);
		String actual = retriever.buildURL("http://test/t/", "GDP", new String[]{"a1", "a2"} , new RetrieveArgument(1990.0, 2000.0, 10));
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void test_buildURL_num()
	{
		String expected = "http://test/t/GDP/json?f=a1,a2&n=3&r=1990.0,2000.0";

		JSONPTableRetriever retriever = new JSONPTableRetrieverForTest(null);
		String actual = retriever.buildURL("http://test/t/", "GDP", new String[]{"a1", "a2"} , new RetrieveArgument(1990.0 ,2000.0, 3));
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void test_buildURL_noFields()
	{
		String expected = "http://test/t/GDP/json?n=3&r=1990.0,2000.0";

		JSONPTableRetriever retriever = new JSONPTableRetrieverForTest(null);
		String actual = retriever.buildURL("http://test/t/", "GDP", new String[]{} , new RetrieveArgument(1990.0, 2000.0, 3));
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void test_RetrieveArgument_equals()
	{
		RetrieveArgument arg1 = new RetrieveArgument();
		RetrieveArgument arg2 = new RetrieveArgument();
		assertEquals(arg1, arg2);
	}
	
	@Test
	public void test_RetrieveArgument_equals_withval()
	{
		RetrieveArgument arg1 = new RetrieveArgument(1980.0, 2000.0, 3);
		RetrieveArgument arg2 = new RetrieveArgument(1980.0, 2000.0, 3);
		assertEquals(arg1, arg2);
	}
	
	@Test
	public void test_RetrieveArgument_equals_notSameBegin()
	{
		RetrieveArgument arg1 = new RetrieveArgument(1980.0, 2000.0, 3);
		RetrieveArgument arg2 = new RetrieveArgument(1990.0, 2000.0, 3);
		assertNotSame(arg1, arg2);
	}
	
	@Test
	public void test_RetrieveArgument_equals_notSameEnd()
	{
		RetrieveArgument arg1 = new RetrieveArgument(1980.0, 2000.0, 3);
		RetrieveArgument arg2 = new RetrieveArgument(1980.0, 2001.0, 3);
		assertNotSame(arg1, arg2);
	}
	@Test
	public void test_RetrieveArgument_equals_notSameNum()
	{
		RetrieveArgument arg1 = new RetrieveArgument(1980.0, 2000.0, 3);
		RetrieveArgument arg2 = new RetrieveArgument(1980.0, 2000.0, 4);
		assertNotSame(arg1, arg2);
	}
	
	ForestNode<QObjectForestAdapter> nextWithTrailing(ForestIterater<QObjectForestAdapter> iter)
	{
		ForestNode<QObjectForestAdapter> node = iter.next();
		while(node.getEdge() != ForestNode.Edge.Trailing)
			node = iter.next();
		return node;
	}

	QObject createNumeric(int i)
	{
		return QInterpreterTest.createNumeric(i);
	}
	
	@Test
	public void test_ForestIterater_flat()
	{
		QObject r2 = createNumeric(2);
		QObject r3 = createNumeric(3);
		
		QObject r = createNumeric(1);
		r.set(1, r2);
		r.set(2, r3);

		ForestIterater<QObjectForestAdapter> iter = QFunction.createForestIterater(r);
		assertEquals(true, iter.hasNext());
		
		ForestNode<QObjectForestAdapter> node = nextWithTrailing(iter);
		assertQNumericEquals(1, node.getElement()._self);
		
		assertEquals(true, iter.hasNext());
		node = nextWithTrailing(iter);
		assertQNumericEquals(2, node.getElement()._self);
		
		assertEquals(true, iter.hasNext());
		node = nextWithTrailing(iter);
		assertQNumericEquals(3, node.getElement()._self);
		
		// root remains
		assertEquals(true, iter.hasNext());		
		nextWithTrailing(iter);
		
		assertEquals(false, iter.hasNext());
	}

	@Test
	public void test_ForestIterater_2level()
	{
		QObject r2 = createNumeric(2);
		QObject r3 = createNumeric(3);
		QObject r4 = createNumeric(4);
		
		QObject r = createNumeric(1);
		r2.set(1, r4);
		r.set(1, r2);
		r.set(2, r3);
		
		// c(1, c(2, 4), 3)

		ForestIterater<QObjectForestAdapter> iter = QFunction.createForestIterater(r);
		assertEquals(true, iter.hasNext());
		
		ForestNode<QObjectForestAdapter> node = nextWithTrailing(iter);
		assertQNumericEquals(1, node.getElement()._self);
		
		node = nextWithTrailing(iter);
		assertQNumericEquals(2, node.getElement()._self);
		
		node = nextWithTrailing(iter);
		assertQNumericEquals(4, node.getElement()._self);
		
		// skip trailing c(2, 4)
		node = nextWithTrailing(iter);
		
		node = nextWithTrailing(iter);
		assertQNumericEquals(3, node.getElement()._self);
		
		
		// root remains
		assertEquals(true, iter.hasNext());		
		nextWithTrailing(iter);
		
		assertEquals(false, iter.hasNext());
	}
	
}
