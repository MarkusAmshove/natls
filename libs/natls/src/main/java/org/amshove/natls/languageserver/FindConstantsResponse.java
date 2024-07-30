package org.amshove.natls.languageserver;

import java.util.List;

@SuppressWarnings("all")
public class FindConstantsResponse
{
	private List<FoundConstant> constants;

	public List<FoundConstant> getConstants()
	{
		return constants;
	}

	public void setConstants(List<FoundConstant> constants)
	{
		this.constants = constants;
	}

	public record FoundConstant(String name, String source)
	{}
}
