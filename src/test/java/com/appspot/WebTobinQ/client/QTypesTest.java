package com.appspot.WebTobinQ.client;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;
import static com.appspot.WebTobinQ.client.QObject.createNumeric;
import static com.appspot.WebTobinQ.client.QObject.createCharacter;

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
		QObject xvector = l.getBB(createNumeric(0));
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
		QObject args = QList.createList();
		QObject x = createVector12("x");
		QObject y = createVector12("y");
		args.set(0, x);
		args.set(1, y);		
		QList df = QList.createDataFrameFromVector(args);
		
		assertEquals(y, df.getBBInt(1));
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
		x.setAttribute("names", QObject.createCharacter(name));
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
