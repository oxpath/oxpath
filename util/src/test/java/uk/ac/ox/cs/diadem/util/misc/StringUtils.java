package uk.ac.ox.cs.diadem.util.misc;

import static java.lang.Math.min;

public class StringUtils {

	public static String maxCommonSuffix(final String string1, final String string2) {
		final char[] chars1 = string1.toCharArray();
		final char[] chars2 = string2.toCharArray();
		int maxCommonLength = min(chars1.length, chars2.length);
		int i;
		for(i = 1 ; i <= maxCommonLength && chars1[chars1.length - i] == chars2[chars2.length - i]; ++i); 
		return string1.substring(chars1.length - i + 1);
	}

 public static String maxCommonPrefix(final String string1, final String string2) {
		final char[] chars1 = string1.toCharArray();
		final char[] chars2 = string2.toCharArray();
		int maxCommonLength = min(chars1.length, chars2.length);
		int i;
		for(i = 0 ; i < maxCommonLength && chars1[i] == chars2[i]; ++i); 
		return string1.substring(0, i);
	}
 
 public static String reverse(final String string) {
	 final char[] chars = string.toCharArray();
	 final char[] reverseChars = new char[chars.length];
	 int j = chars.length - 1;
	 for(int i = 0 ; i < chars.length; ++i, --j) 
		 reverseChars[j] = chars[i];
	 return new String(reverseChars);
 }

}
