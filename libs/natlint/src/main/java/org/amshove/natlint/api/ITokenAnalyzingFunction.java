package org.amshove.natlint.api;

import org.amshove.natparse.lexing.SyntaxToken;

@FunctionalInterface
public interface ITokenAnalyzingFunction
{
	void analyze(SyntaxToken token, IAnalyzeContext context);
}
