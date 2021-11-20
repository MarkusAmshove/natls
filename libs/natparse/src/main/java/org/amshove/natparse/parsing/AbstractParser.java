package org.amshove.natparse.parsing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.TokenList;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractParser<T>
{
	protected TokenList tokens;
	protected List<IDiagnostic> diagnostics;

	public ParseResult<T> parse(TokenList tokens)
	{
		this.tokens = tokens;
		diagnostics = new ArrayList<>();

		var result = parseInternal();

		return new ParseResult(result, ReadOnlyList.from(diagnostics));
	}

	protected abstract T parseInternal();
}
