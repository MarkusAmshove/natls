package org.amshove.natlint.linter;

import org.amshove.natlint.api.*;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.ITokenNode;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum LinterContext implements ILinterContext
{
	INSTANCE;

	private boolean initialized = false;
	private List<AbstractAnalyzer> registeredAnalyzers;
	private final Map<Class<? extends ISyntaxNode>, List<INodeAnalyzingFunction>> nodeAnalyzerFunctions = new HashMap<>();
	private final Map<SyntaxKind, List<ITokenAnalyzingFunction>> tokenAnalyzerFunctions = new HashMap<>();

	LinterContext()
	{
		reinitialize();
	}

	void reinitialize() // used for tests only :(
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
	public void registerNodeAnalyzer(Class<? extends ISyntaxNode> nodeType, INodeAnalyzingFunction analyzingFunction)
	{
		nodeAnalyzerFunctions.computeIfAbsent(nodeType, n -> new ArrayList<>())
			.add(analyzingFunction);
	}

	@Override
	public void registerTokenAnalyzer(SyntaxKind kind, ITokenAnalyzingFunction analyzingFunction)
	{
		tokenAnalyzerFunctions.computeIfAbsent(kind, k -> new ArrayList<>())
			.add(analyzingFunction);
	}

	void analyze(ISyntaxNode syntaxNode, IAnalyzeContext context)
	{
		var analyzers = nodeAnalyzerFunctions.get(syntaxNode.getClass());
		if (analyzers != null)
		{
			analyzers.forEach(analyzer -> analyzer.analyze(syntaxNode, context));
		}

		if(syntaxNode instanceof ITokenNode tokenNode)
		{
			var tokenAnalyzer = tokenAnalyzerFunctions.get(tokenNode.token().kind());
			if(tokenAnalyzer != null)
			{
				tokenAnalyzer.forEach(a -> a.analyze(tokenNode.token(), context));
			}
		}

		nodeAnalyzerFunctions.entrySet().stream().filter(e -> e.getKey().isAssignableFrom(syntaxNode.getClass()))
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
		nodeAnalyzerFunctions.clear();
		initialized = false;
	}

	/* package */ ReadOnlyList<AbstractAnalyzer> registeredAnalyzers()
	{
		return ReadOnlyList.from(registeredAnalyzers);
	}
}
