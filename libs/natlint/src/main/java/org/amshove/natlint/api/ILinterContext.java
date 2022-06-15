package org.amshove.natlint.api;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.ISyntaxNode;

public interface ILinterContext
{
	void registerNodeAnalyzer(Class<? extends ISyntaxNode> nodeType, INodeAnalyzingFunction analyzingFunction);

	void registerTokenAnalyzer(SyntaxKind kind, ITokenAnalyzingFunction analyzingFunction);
}
