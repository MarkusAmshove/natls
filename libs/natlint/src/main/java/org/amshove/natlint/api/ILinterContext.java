package org.amshove.natlint.api;

import org.amshove.natparse.natural.ISyntaxNode;

public interface ILinterContext
{
	void registerNodeAnalyzer(Class<? extends ISyntaxNode> nodeType, IAnalyzingFunction analyzingFunction);
}
