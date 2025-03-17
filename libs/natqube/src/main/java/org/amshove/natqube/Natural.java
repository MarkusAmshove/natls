package org.amshove.natqube;

import org.sonar.api.resources.AbstractLanguage;

public class Natural extends AbstractLanguage
{
	public static final String KEY = "natural";
	public static final String NAME = "Natural";

	private static String[] fileSuffixes;

	public Natural()
	{
		super(KEY, NAME);
	}

	@Override
	public String[] getFileSuffixes()
	{
		return fileSuffixes();
	}

	public static String[] fileSuffixes()
	{
		if (fileSuffixes == null)
		{
			fileSuffixes = NaturalModuleType.allFileExtensions().stream().map(extension -> "." + extension).toArray(String[]::new);
		}

		return fileSuffixes;
	}
}
