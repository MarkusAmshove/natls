package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NatUnitTestNameAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription DUPLICATED_TEST_NAME = DiagnosticDescription.create(
		"NL008",
		"Test with the same name is already defined in line %d",
		DiagnosticSeverity.ERROR
	);

	private Map<INaturalModule, Map<String, Integer>> definedTestCases;

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
	public void beforeAnalyzing(IAnalyzeContext context)
	{
		definedTestCases = new ConcurrentHashMap<>();
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

		var possibleTestReference = ifStatement.descendants().get(1);
		if (!(possibleTestReference instanceof ISymbolReferenceNode symbolReferenceNode && symbolReferenceNode.referencingToken().symbolName().equals("NUTESTP.TEST")))
		{
			return;
		}

		var possibleTestName = ifStatement.descendants().get(3);
		if (!(possibleTestName instanceof ITokenNode nameToken) || nameToken.token().kind() != SyntaxKind.STRING_LITERAL)
		{
			return;
		}

		var testName = nameToken.token().stringValue();
		if (definedTestCases.containsKey(context.getModule()) && definedTestCases.get(context.getModule()).containsKey(testName))
		{
			var line = definedTestCases.get(context.getModule()).get(testName);
			context.report(DUPLICATED_TEST_NAME.createFormattedDiagnostic(nameToken.token(), line));
		}
		else
		{
			markTest(context.getModule(), testName, nameToken.token().line());
		}
	}

	private void markTest(INaturalModule module, String testName, int line)
	{
		var testCasesInModule = definedTestCases.computeIfAbsent(module, m -> new ConcurrentHashMap<>());
		testCasesInModule.put(testName, line);
	}
}
