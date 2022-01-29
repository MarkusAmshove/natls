package org.amshove.natlint.linter;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.IAnalyzingFunction;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.natural.ISyntaxNode;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum LinterContext implements ILinterContext
{
	INSTANCE;

	private boolean initialized = false;
	private final List<AbstractAnalyzer> registeredAnalyzers;
	private final Map<Class<? extends ISyntaxNode>, List<IAnalyzingFunction>> analyzerFunctions = new HashMap<>();

	LinterContext()
	{
		var reflections = new Reflections("org.amshove.natlint.analyzers");
		var analyzers = new ArrayList<AbstractAnalyzer>();
		for (var analyzerClass : reflections.getSubTypesOf(AbstractAnalyzer.class))
		{
			try
			{
				var analyzer = analyzerClass.getConstructor().newInstance();
				analyzers.add(analyzer);
			}
			catch (Exception e)
			{
				throw new RuntimeException(
					"Analyzer %s can not be instantiated. Does it have a parameterless constructor?".formatted(analyzerClass.getName()),
					e
				);
			}
		}

		registeredAnalyzers = analyzers;
	}

	void registerAnalyzer(AbstractAnalyzer analyzer)
	{
		registeredAnalyzers.add(analyzer);
	}

	@Override
	public void registerNodeAnalyzer(Class<? extends ISyntaxNode> nodeType, IAnalyzingFunction analyzingFunction)
	{
		analyzerFunctions.computeIfAbsent(nodeType, n -> new ArrayList<>())
			.add(analyzingFunction);
	}

	void analyze(ISyntaxNode syntaxNode, IAnalyzeContext context)
	{
		var analyzers = analyzerFunctions.get(syntaxNode.getClass());
		if (analyzers != null)
		{
			analyzers.forEach(analyzer -> analyzer.analyze(syntaxNode, context));
		}

		analyzerFunctions.entrySet().stream().filter(e -> e.getKey().isAssignableFrom(syntaxNode.getClass()))
			.forEach(e -> e.getValue().forEach(a -> a.analyze(syntaxNode, context)));
	}

	void beforeAnalyzing(IAnalyzeContext context)
	{
		if (!initialized)
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
