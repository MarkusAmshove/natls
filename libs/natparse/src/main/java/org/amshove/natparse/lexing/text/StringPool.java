package org.amshove.natparse.lexing.text;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

class StringPool
{
	private static final Interner<String> CACHE = Interners.newWeakInterner();

	static String get(String string)
	{
		return CACHE.intern(string);
	}
}
