package org.amshove.natlint.api;

import org.amshove.natparse.natural.ISyntaxNode;

import java.util.function.BiConsumer;

public interface ILinterContext
{
	void registerNodeAnalyzer(Class<? extends ISyntaxNode> nodeType, BiConsumer<ISyntaxNode, IAnalyzeContext> context);
}
