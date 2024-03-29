package org.amshove.natlint.linter;

import org.amshove.natlint.api.*;
import org.amshove.natlint.editorconfig.EditorConfig;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.ITokenNode;
import org.reflections.Reflections;

import java.util.*;

public enum LinterContext implements ILinterContext
{
	INSTANCE;

	private boolean initialized = false;
	private List<AbstractAnalyzer> registeredAnalyzers;
	private final Map<Class<? extends ISyntaxNode>, List<INodeAnalyzingFunction>> nodeAnalyzerFunctions = new HashMap<>();
	private final Map<SyntaxKind, List<ITokenAnalyzingFunction>> tokenAnalyzerFunctions = new EnumMap<>(SyntaxKind.class);
	private final List<IModuleAnalyzingFunction> moduleAnalyzerFunctions = new ArrayList<>();

	private EditorConfig editorConfig;

	LinterContext()
	{
		reinitialize();
	}

	void reinitialize()
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
		initializeAnalyzers();
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

	@Override
	public void registerModuleAnalyzer(IModuleAnalyzingFunction analyzingFunction)
	{
		moduleAnalyzerFunctions.add(analyzingFunction);
	}

	public void updateEditorConfig(EditorConfig config)
	{
		editorConfig = config;
	}

	void analyze(ISyntaxNode syntaxNode, IAnalyzeContext context)
	{
		var analyzers = nodeAnalyzerFunctions.get(syntaxNode.getClass());
		if (analyzers != null)
		{
			analyzers.forEach(analyzer -> analyzer.analyze(syntaxNode, context));
		}

		if (syntaxNode instanceof ITokenNode tokenNode && tokenNode.token() != null)
		{
			var tokenAnalyzer = tokenAnalyzerFunctions.get(tokenNode.token().kind());
			if (tokenAnalyzer != null)
			{
				tokenAnalyzer.forEach(a -> a.analyze(tokenNode.token(), context));
			}
		}

		nodeAnalyzerFunctions.entrySet().stream().filter(e -> e.getKey().isAssignableFrom(syntaxNode.getClass()))
			.forEach(e -> e.getValue().forEach(a -> a.analyze(syntaxNode, context)));
	}

	void analyzeModule(INaturalModule module, IAnalyzeContext context)
	{
		moduleAnalyzerFunctions.forEach(a -> a.analyze(module, context));
	}

	void beforeAnalyzing(IAnalyzeContext context)
	{
		registeredAnalyzers.forEach(a -> a.beforeAnalyzing(context));
	}

	void afterAnalyzing(IAnalyzeContext context)
	{
		registeredAnalyzers.forEach(a -> a.afterAnalyzing(context));
	}

	public Optional<EditorConfig> editorConfig()
	{
		return Optional.ofNullable(editorConfig);
	}

	/* test */ void reset()
	{
		registeredAnalyzers.clear();
		nodeAnalyzerFunctions.clear();
		tokenAnalyzerFunctions.clear();
		moduleAnalyzerFunctions.clear();
		editorConfig = null;
		initialized = false;
	}

	/* test */ void initializeAnalyzers()
	{
		if (!initialized)
		{
			registeredAnalyzers.forEach(a -> a.initialize(this));
			initialized = true;
		}
	}

	public ReadOnlyList<AbstractAnalyzer> registeredAnalyzers()
	{
		return ReadOnlyList.from(registeredAnalyzers);
	}

	/* test */ void registerAnalyzer(AbstractAnalyzer analyzer)
	{
		registeredAnalyzers.add(analyzer);
	}
}
