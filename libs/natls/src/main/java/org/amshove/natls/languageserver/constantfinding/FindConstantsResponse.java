package org.amshove.natls.languageserver.constantfinding;

import java.util.List;

@SuppressWarnings("all")
public class FindConstantsResponse
{
	private List<FoundConstant> constants;

	public FindConstantsResponse()
	{
		constants = List.of();
	}

	public List<FoundConstant> getConstants()
	{
		return constants;
	}

	public void setConstants(List<FoundConstant> constants)
	{
		this.constants = constants;
	}

}
