package org.amshove.natlint.api;

import org.amshove.natparse.natural.ISyntaxNode;

@FunctionalInterface
public interface IAnalyzingFunction
{
	void analyze(ISyntaxNode node, IAnalyzeContext context);
}
