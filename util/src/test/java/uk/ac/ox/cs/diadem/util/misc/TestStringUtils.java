package uk.ac.ox.cs.diadem.util.misc;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import uk.ac.ox.cs.diadem.util.testsupport.StandardTestcase;

public class TestStringUtils extends StandardTestcase {
	
	private void performPrefixCheck(final String s1, final String s2) {
		assertTrue(database.check("'"+s1+"'/'"+s2+"'", StringUtils.maxCommonPrefix(s1,s2)));
	}
	
	@Test
	public void testMaxCommonPrefix() {
		database.setMethodKeyPrefix();
		performPrefixCheck("","");
		performPrefixCheck("xx","xx");
		performPrefixCheck("xx","x");
		performPrefixCheck("x","xx");
		performPrefixCheck("","xyz");
		performPrefixCheck("xyz","");
		performPrefixCheck("abcd","abxcd");
		performPrefixCheck("abcd","abcdef");
	}

	private void performSuffixCheck(final String s1, final String s2) {
		assertTrue(database.check("'"+s1+"'/'"+s2+"'", StringUtils.maxCommonSuffix(s1,s2)));
	}
	
	@Test
	public void testMaxCommonSuffix() {
		database.setMethodKeyPrefix();
		performPrefixCheck("","");
		performPrefixCheck("xx","xx");
		performPrefixCheck("xx","x");
		performPrefixCheck("x","xx");
		performPrefixCheck("","xyz");
		performPrefixCheck("xyz","");
		performSuffixCheck("dcba","dcxba");
		performSuffixCheck("dcba","efdcba");
	}

	private void performReverseCheck(final String s) {
		assertTrue(database.check("'"+s+"'", StringUtils.reverse(s)));
	}

	@Test
	public void testReverse() {
		database.setMethodKeyPrefix();
		performReverseCheck("");
		performReverseCheck("1");
		performReverseCheck("12");
		performReverseCheck("121");
		performReverseCheck("1234");
		
	}
	
}
