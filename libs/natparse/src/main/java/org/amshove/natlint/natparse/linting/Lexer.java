package org.amshove.natlint.natparse.linting;

import java.util.ArrayList;
import java.util.List;

public class Lexer
{
	public List<SyntaxToken> lex(String source)
	{
		List<SyntaxToken> tokens = new ArrayList<>();
		tokens.add(new SyntaxToken(SyntaxKind.NEW_LINE, 0, 0, 0, source));
		return tokens;
	}
}
