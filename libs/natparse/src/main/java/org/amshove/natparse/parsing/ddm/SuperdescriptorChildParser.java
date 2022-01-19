package org.amshove.natparse.parsing.ddm;

import org.amshove.natparse.NaturalParseException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SuperdescriptorChildParser
{
	private static final Pattern SUPERDESCRIPTOR_CHILD_PATTERN = Pattern.compile("\\*\\s*(?<NAME>[^(]*)\\((?<RANGEFROM>\\d+)-(?<RANGETO>\\d+)\\)\\s*");

	public SuperdescriptorChild parse(String line)
	{
		var matcher = SUPERDESCRIPTOR_CHILD_PATTERN.matcher(line);
		if (!matcher.matches())
		{
			throw new NaturalParseException(String.format("Can't parse Superdescriptorchild from \"%s\"", line));
		}

		return new SuperdescriptorChild(getName(matcher), getRangeFrom(matcher), getRangeTo(matcher));
	}

	private static String getName(Matcher matcher)
	{
		return matcher.group("NAME").trim();
	}

	private static int getRangeFrom(Matcher matcher)
	{
		return Integer.parseInt(matcher.group("RANGEFROM"));
	}

	private static int getRangeTo(Matcher matcher)
	{
		return Integer.parseInt(matcher.group("RANGETO"));
	}
}
