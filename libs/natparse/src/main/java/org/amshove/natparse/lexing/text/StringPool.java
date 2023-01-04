package org.amshove.natparse.lexing.text;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

/**
 * Used to pool strings within the heap. Using this for Strings that can appear often, like keywords, heavily reduced
 * the load on the heap.
 */
public class StringPool
{
	private static final Interner<String> CACHE = Interners.newWeakInterner();

	public static String intern(String string)
	{
		return CACHE.intern(string);
	}
}
