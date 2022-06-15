package org.amshove.natlint.api;

import org.amshove.natparse.natural.ISyntaxNode;

@FunctionalInterface
public interface INodeAnalyzingFunction
{
	void analyze(ISyntaxNode node, IAnalyzeContext context);
}
