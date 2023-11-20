package org.amshove.natparse;

public class StringUtil
{
	private StringUtil()
	{

	}

	public static boolean isAllDigits(String str)
	{
		if (str.isEmpty() || str.isBlank())
		{
			return false;
		}

		for (var i = 0; i < str.length(); i++)
		{
			var c = str.charAt(i);
			if (!Character.isDigit(c))
			{
				return false;
			}
		}

		return true;
	}
}
