package com.appspot.WebTobinQ.client;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;

import org.junit.Test;


public class RTypesTest {
	@Test
	public void test_RInt_equals()
	{
		RInt a = new RInt(5);
		RInt b = new RInt(5);
		assertEquals(a, b);
		
	}
	
	@Test
	public void test_RInt_equals_multiple()
	{
		RInt r1 = new RInt(1);
		RInt r2 = new RInt(2);
		
		RInt v1 = new RInt();
		v1.set(0, r1);
		v1.set(1, r2);
		
		RInt v2 = new RInt();
		v2.set(0, r1);
		v2.set(1, r2);
		
		assertEquals(v1, v2);
	}
	
	@Test
	public void test_RInt_equals_onlyFirstEqual()
	{
		RInt r1 = new RInt(1);
		RInt r2 = new RInt(2);
		
		RInt v1 = new RInt();
		v1.set(0, r1);
		v1.set(1, r2);
				
		assertNotSame(v1, r1);
	}
	
	@Test
	public void test_RInt_notEquals()
	{
		RInt a = new RInt(5);
		RInt b = new RInt(6);
		assertNotSame(a, b);
	}
	
	@Test
	public void test_RInt_set()
	{
		RInt a = new RInt(1);
		a.set(2, new RInt(3));

		
		assertEquals(3, a.getLength());
		assertEquals(new RInt(1), a.get(0));
		assertEquals(RObject.NA, a.get(1));
		assertEquals(new RInt(3), a.get(2));
		
	}
	
	@Test
	public void test_RInt_recycle()
	{
		RInt r1 = new RInt(1);
		RInt r2 = new RInt(2);
		RInt r3 = new RInt(3);
		
		RInt r = new RInt(1);
		r.set(1, r2);
		r.set(2, r3);
		
		RObject recycle = r.recycle(7);
		assertEquals(r1, recycle.get(0));
		assertEquals(r2, recycle.get(1));
		assertEquals(r3, recycle.get(2));
		assertEquals(r1, recycle.get(3));
		assertEquals(r2, recycle.get(4));
		assertEquals(r3, recycle.get(5));
		assertEquals(r1, recycle.get(6));
	}

}
