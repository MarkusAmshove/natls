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
import org.amshove.natparse.natural.ITokenNode;
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

	public static final Map<SyntaxKind, String> PREFERRED_OPERATOR_SIGNS = Map.of(
		SyntaxKind.GT, ">",
		SyntaxKind.LT, "<",
		SyntaxKind.EQ, "=",
		SyntaxKind.NE, "<>",
		SyntaxKind.GE, ">=",
		SyntaxKind.LE, "<="
	);

	public static final Map<SyntaxKind, String> PREFERRED_OPERATOR_SHORT = Map.of(
		SyntaxKind.GREATER_SIGN, "GT",
		SyntaxKind.LESSER_SIGN, "LT",
		SyntaxKind.EQUALS_SIGN, "EQ",
		SyntaxKind.LESSER_GREATER, "NE",
		SyntaxKind.GREATER_EQUALS_SIGN, "GE",
		SyntaxKind.LESSER_EQUALS_SIGN, "LE"
	);

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(DISCOURAGED_BOOLEAN_OPERATOR, INVALID_NATUNIT_COMPARISON_OPERATOR);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		PREFERRED_OPERATOR_SIGNS.keySet().forEach(sk -> context.registerTokenAnalyzer(sk, this::analyzeToken));
		PREFERRED_OPERATOR_SHORT.keySet().forEach(sk -> context.registerTokenAnalyzer(sk, this::analyzeToken));
		context.registerTokenAnalyzer(SyntaxKind.EQUALS_SIGN, this::analyzeEquals);
	}

	private void analyzeToken(SyntaxToken syntaxToken, IAnalyzeContext context)
	{
		if (context.getModule().isTestCase())
		{
			return;
		}

		var configuredPreference = context.getConfiguration(context.getModule().file(), "natls.operators", "sign");
		var preferredOperatorMapping = configuredPreference.equalsIgnoreCase("short") ? PREFERRED_OPERATOR_SHORT : PREFERRED_OPERATOR_SIGNS;

		if (!preferredOperatorMapping.containsKey(syntaxToken.kind()))
		{
			return;
		}

		var preferredOperator = preferredOperatorMapping.get(syntaxToken.kind());
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
