package org.amshove.natlint.linter;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.natural.ISyntaxNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public enum LinterContext implements ILinterContext
{
	INSTANCE;

	private boolean initialized = false;
	private final List<AbstractAnalyzer> registeredAnalyzers = new ArrayList<>();
	private final Map<Class<? extends ISyntaxNode>, List<BiConsumer<ISyntaxNode, IAnalyzeContext>>> analyzerFunctions = new HashMap<>();

	public void registerAnalyzer(AbstractAnalyzer analyzer)
	{
		registeredAnalyzers.add(analyzer);
	}

	@Override
	public void registerNodeAnalyzer(Class<? extends ISyntaxNode> nodeType, BiConsumer<ISyntaxNode, IAnalyzeContext> analyzerFunction)
	{
		analyzerFunctions.computeIfAbsent(nodeType, n -> new ArrayList<>())
			.add(analyzerFunction);
	}

	void analyze(ISyntaxNode syntaxNode, IAnalyzeContext context)
	{
		var analyzers = analyzerFunctions.get(syntaxNode.getClass());
		if (analyzers != null)
		{
			analyzers.forEach(analyzer -> analyzer.accept(syntaxNode, context));
		}

		analyzerFunctions.entrySet().stream().filter(e -> e.getKey().isAssignableFrom(syntaxNode.getClass()))
			.forEach(e -> e.getValue().forEach(a -> a.accept(syntaxNode, context)));
	}

	void beforeAnalyzing(IAnalyzeContext context)
	{
		if(!initialized)
		{
			registeredAnalyzers.forEach(a -> a.initialize(this));
			initialized = true;
		}

		registeredAnalyzers.forEach(a -> a.beforeAnalyzing(context));
	}

	void afterAnalyzing(IAnalyzeContext context)
	{
		registeredAnalyzers.forEach(a -> a.afterAnalyzing(context));
	}

	void reset()
	{
		registeredAnalyzers.clear();
		analyzerFunctions.clear();
		initialized = false;
	}
}
