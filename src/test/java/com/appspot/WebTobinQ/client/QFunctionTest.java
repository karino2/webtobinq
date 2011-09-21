package com.appspot.WebTobinQ.client;

import static com.appspot.WebTobinQ.client.QInterpreterTest.createNumeric;
import static com.appspot.WebTobinQ.client.QInterpreterTest.assertQCharEquals;
import static com.appspot.WebTobinQ.client.QInterpreterTest.assertQNumericEquals;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.junit.Test;

import com.appspot.WebTobinQ.client.QFunction.QObjectForestAdapter;
import com.appspot.WebTobinQ.client.TableRetrievable.RetrieveArgument;


public class QFunctionTest {
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
		QObject i = createNumeric(0);
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
