package com.appspot.WebTobinQ.client;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;

import org.junit.Test;


public class QTypesTest {
	@Test
	public void test_RInt_equals()
	{
		QInt a = new QInt(5);
		QInt b = new QInt(5);
		assertEquals(a, b);
		
	}
	
	@Test
	public void test_RInt_equals_multiple()
	{
		QInt r1 = new QInt(1);
		QInt r2 = new QInt(2);
		
		QInt v1 = new QInt();
		v1.set(0, r1);
		v1.set(1, r2);
		
		QInt v2 = new QInt();
		v2.set(0, r1);
		v2.set(1, r2);
		
		assertEquals(v1, v2);
	}
	
	@Test
	public void test_RInt_equals_onlyFirstEqual()
	{
		QInt r1 = new QInt(1);
		QInt r2 = new QInt(2);
		
		QInt v1 = new QInt();
		v1.set(0, r1);
		v1.set(1, r2);
				
		assertNotSame(v1, r1);
	}
	
	@Test
	public void test_RInt_notEquals()
	{
		QInt a = new QInt(5);
		QInt b = new QInt(6);
		assertNotSame(a, b);
	}
	
	@Test
	public void test_RInt_set()
	{
		QInt a = new QInt(1);
		a.set(2, new QInt(3));

		
		assertEquals(3, a.getLength());
		assertEquals(new QInt(1), a.get(0));
		assertEquals(QObject.NA, a.get(1));
		assertEquals(new QInt(3), a.get(2));
		
	}
	
	@Test
	public void test_RInt_recycle()
	{
		QInt r1 = new QInt(1);
		QInt r2 = new QInt(2);
		QInt r3 = new QInt(3);
		
		QInt r = new QInt(1);
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

}
