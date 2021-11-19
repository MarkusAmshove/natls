package org.amshove.natparse.parsing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.TokenList;
import org.amshove.natparse.natural.IDefineData;

import java.util.ArrayList;
import java.util.List;

public class DefineDataParser
{
	private List<IDiagnostic> diagnostics;

	public ParseResult<IDefineData> parseDefineData(TokenList tokens)
	{
		diagnostics = new ArrayList<>();
		advanceToDefineData(tokens);
		if(!isAtStartOfDefineData(tokens))
		{
			diagnostics.add(ParserDiagnostic.create("DEFINE DATA expected", 0, 0, 0, 0, ParserError.NO_DEFINE_DATA_FOUND));
			return new ParseResult<IDefineData>(null, ReadOnlyList.from(diagnostics));
		}


		while(!tokens.isAtEnd() && tokens.peek().kind() != SyntaxKind.END_DEFINE)
		{
			/*
			magic
			 */
			tokens.advance();
		}

		if(tokens.isAtEnd())
		{
			diagnostics.add(ParserDiagnostic.create("No END-DEFINE found", tokens.peek(-1), ParserError.MISSING_END_DEFINE));
			return new ParseResult<IDefineData>(null, ReadOnlyList.from(diagnostics));
		}

		return new ParseResult(new DefineData(), ReadOnlyList.from(diagnostics));
	}

	private static void advanceToDefineData(TokenList tokens)
	{
		while(!tokens.isAtEnd() && tokens.peek().kind() != SyntaxKind.DEFINE)
		{
			tokens.advance();
		}
	}

	private static boolean isAtStartOfDefineData(TokenList tokens)
	{
		return !tokens.isAtEnd() && tokens.peek().kind() == SyntaxKind.DEFINE && tokens.peek(1) != null && tokens.peek(1).kind() == SyntaxKind.DATA;
	}
}
