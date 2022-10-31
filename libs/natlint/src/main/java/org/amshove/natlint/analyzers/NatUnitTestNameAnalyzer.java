package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.conditionals.IRelationalCriteriaNode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NatUnitTestNameAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription DUPLICATED_TEST_NAME = DiagnosticDescription.create(
		"NL008",
		"Test with the same name is already defined in line %d",
		DiagnosticSeverity.ERROR
	);

	private final Map<INaturalModule, Map<String, Integer>> definedTestCases = new ConcurrentHashMap<>();

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(DUPLICATED_TEST_NAME);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerNodeAnalyzer(IIfStatementNode.class, this::analyzeTestName);
	}

	@Override
	public void afterAnalyzing(IAnalyzeContext context)
	{
		definedTestCases.remove(context.getModule());
	}

	private void analyzeTestName(ISyntaxNode node, IAnalyzeContext context)
	{
		if (!context.getModule().isTestCase())
		{
			return;
		}

		var ifStatement = (IIfStatementNode) node;

		var condition = ifStatement.condition();
		if (!(condition.criteria() instanceof IRelationalCriteriaNode relationalNode))
		{
			return;
		}

		if (!(relationalNode.left() instanceof ISymbolReferenceNode testedVariable))
		{
			return;
		}

		if (!testedVariable.referencingToken().symbolName().equals("NUTESTP.TEST"))
		{
			return;
		}

		if (!(relationalNode.right() instanceof ILiteralNode nameNode))
		{
			return;
		}

		var nameToken = nameNode.token();
		var testName = nameToken.stringValue();
		if (definedTestCases.containsKey(context.getModule()) && definedTestCases.get(context.getModule()).containsKey(testName))
		{
			var line = definedTestCases.get(context.getModule()).get(testName) + 1; // line numbers are 0 based
			context.report(DUPLICATED_TEST_NAME.createFormattedDiagnostic(nameToken, line));
		}
		else
		{
			markTest(context.getModule(), testName, nameToken.line());
		}
	}

	private void markTest(INaturalModule module, String testName, int line)
	{
		var testCasesInModule = definedTestCases.computeIfAbsent(module, m -> new ConcurrentHashMap<>());
		testCasesInModule.put(testName, line);
	}
}
