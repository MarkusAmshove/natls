package org.amshove.natparse.parsing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;

public class ParseResult<T>
{
	private final T result;
	private final ReadOnlyList<IDiagnostic> diagnostics;

	public ParseResult(T result, ReadOnlyList<IDiagnostic> diagnostics)
	{
		this.result = result;
		this.diagnostics = diagnostics;
	}

	public T result()
	{
		return this.result;
	}

	public ReadOnlyList<IDiagnostic> diagnostics()
	{
		return this.diagnostics;
	}
}
