package com.appspot.WebTobinQ.client;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;

import org.junit.Test;


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
}
