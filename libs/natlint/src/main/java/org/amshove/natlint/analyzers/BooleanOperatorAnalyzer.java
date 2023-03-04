package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.ISymbolReferenceNode;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.ITokenNode;
import org.amshove.natparse.natural.conditionals.IHasComparisonOperator;
import org.amshove.natparse.natural.conditionals.IRelationalCriteriaNode;

import java.util.Map;

public class BooleanOperatorAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription DISCOURAGED_BOOLEAN_OPERATOR = DiagnosticDescription.create(
		"NL006",
		"Operator %s is discouraged, use %s instead",
		DiagnosticSeverity.INFO
	);

	public static final DiagnosticDescription INVALID_NATUNIT_COMPARISON_OPERATOR = DiagnosticDescription.create(
		"NL007",
		"Operator = is not recognized by NatUnit, use EQ instead",
		DiagnosticSeverity.ERROR
	);

	private static final Map<SyntaxKind, String> PREFERRED_OPERATOR_SIGNS = Map.of(
		SyntaxKind.GT, ">",
		SyntaxKind.LT, "<",
		SyntaxKind.EQ, "=",
		SyntaxKind.NE, "<>",
		SyntaxKind.GE, ">=",
		SyntaxKind.LE, "<="
	);

	private static final Map<SyntaxKind, String> PREFERRED_OPERATOR_SHORT = Map.of(
		SyntaxKind.GREATER_SIGN, "GT",
		SyntaxKind.LESSER_SIGN, "LT",
		SyntaxKind.EQUALS_SIGN, "EQ",
		SyntaxKind.LESSER_GREATER, "NE",
		SyntaxKind.GREATER_EQUALS_SIGN, "GE",
		SyntaxKind.LESSER_EQUALS_SIGN, "LE"
	);

	private Map<SyntaxKind, String> preferredOperatorMapping;

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(DISCOURAGED_BOOLEAN_OPERATOR, INVALID_NATUNIT_COMPARISON_OPERATOR);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerNodeAnalyzer(IHasComparisonOperator.class, this::analyzeComparison);
		context.registerTokenAnalyzer(SyntaxKind.EQUALS_SIGN, this::analyzeEquals);
	}

	@Override
	public void beforeAnalyzing(IAnalyzeContext context)
	{
		var configuredPreference = context.getConfiguration(context.getModule().file(), "natls.style.comparisons", "sign");
		preferredOperatorMapping = configuredPreference.equalsIgnoreCase("short") ? PREFERRED_OPERATOR_SHORT : PREFERRED_OPERATOR_SIGNS;
	}

	private void analyzeComparison(ISyntaxNode node, IAnalyzeContext context)
	{
		var comparisonNode = (IHasComparisonOperator) node;
		var syntaxToken = comparisonNode.comparisonToken();

		var preferredOperator = preferredOperatorMapping.get(syntaxToken.kind());

		if (preferredOperator == null)
		{
			return;
		}

		if (preferredOperator.equals(syntaxToken.source()))
		{
			return;
		}

		if (preferredOperator.equals("=") && context.getModule().isTestCase())
		{
			return;
		}

		context.report(
			DISCOURAGED_BOOLEAN_OPERATOR.createFormattedDiagnostic(
				syntaxToken,
				syntaxToken.source(),
				preferredOperator
			)
		);
	}

	private void analyzeEquals(SyntaxToken syntaxToken, IAnalyzeContext context)
	{
		if (!context.getModule().isTestCase())
		{
			return;
		}

		var node = NodeUtil.findNodeAtPosition(syntaxToken.line(), syntaxToken.offsetInLine(), context.getModule());
		if (node == null || !(node.parent()instanceof IRelationalCriteriaNode relationalCriteria))
		{
			return;
		}

		var possibleTestReference = relationalCriteria.left();
		var possibleTestComparisonOperator = relationalCriteria.descendants().get(1);

		if (syntaxToken.kind() == SyntaxKind.EQUALS_SIGN
			&& possibleTestReference instanceof ISymbolReferenceNode symbolReferenceNode && symbolReferenceNode.referencingToken().symbolName().equals("NUTESTP.TEST")
			&& possibleTestComparisonOperator instanceof ITokenNode tokenNode && tokenNode.token() == syntaxToken)
		{
			context.report(INVALID_NATUNIT_COMPARISON_OPERATOR.createDiagnostic(syntaxToken));
		}
	}
}
