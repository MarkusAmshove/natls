package org.amshove.natparse.lexing.text;

import java.util.HashMap;

class StringPool
{
	private static final HashMap<String, String> POOL = new HashMap<>();

	static String get(String string)
	{
		if(!POOL.containsKey(string))
		{
			POOL.put(string, string);
		}

		return POOL.get(string);
	}
}
