package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.IIfStatementNode;
import org.amshove.natparse.natural.ISymbolReferenceNode;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.ITokenNode;

import java.util.HashMap;
import java.util.Map;

public class NatUnitTestNameAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription DUPLICATED_TEST_NAME = DiagnosticDescription.create(
		"NL008",
		"Test with the same name is already defined in line %d",
		DiagnosticSeverity.ERROR
	);

	private Map<String, Integer> definedTestCases;

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
		// TODO: Is this actually safe with all the requests/cancellations going on in a LSP?
		definedTestCases = new HashMap<>();
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
		if (!(possibleTestName instanceof ITokenNode nameToken) || nameToken.token().kind() != SyntaxKind.STRING)
		{
			return;
		}

		var testName = nameToken.token().stringValue();
		if (definedTestCases.containsKey(testName))
		{
			context.report(DUPLICATED_TEST_NAME.createFormattedDiagnostic(nameToken.token(), definedTestCases.get(testName)));
		}
		else
		{
			definedTestCases.put(testName, nameToken.token().line());
		}
	}
}
