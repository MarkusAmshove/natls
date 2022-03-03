package org.amshove.natls;

public enum DiagnosticTool
{
	NATPARSE("NatParse"),
	NATUNIT("NatUnit"),
	NATLINT("NatLint"),
	CATALOG("Natural");

	private final String id;

	DiagnosticTool(String id)
	{
		this.id = id;
	}

	public String getId()
	{
		return id;
	}
}
