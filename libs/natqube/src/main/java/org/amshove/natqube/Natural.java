package org.amshove.natqube;

import org.sonar.api.resources.AbstractLanguage;

public class Natural extends AbstractLanguage
{
	public static final String KEY = "natural";
	public static final String NAME = "Natural";

	public Natural()
	{
		super(KEY, NAME);
	}

	@Override
	public String[] getFileSuffixes()
	{
		return Natural.fileSuffixes();
	}

	// TODO: Create enum and reuse
	public static String[] fileSuffixes()
	{
		return new String[]
		{
			".NSN", ".NSL", ".NSP", ".NSS", ".NSD", ".NSA", ".NSG", ".NSM", ".NSC", ".NS7", ".NSH"
		};
	}
}
