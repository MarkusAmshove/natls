package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;

import java.util.Map;

public class BooleanOperatorAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription DISCOURAGED_BOOLEAN_OPERATOR = DiagnosticDescription.create(
		"NL006",
		"Operator %s is discouraged, use %s instead",
		DiagnosticSeverity.INFO
	);

	public static final Map<SyntaxKind, String> PREFERRED_OPERATORS = Map.of(
		SyntaxKind.GT, ">",
		SyntaxKind.LT, "<",
		SyntaxKind.EQ, "=",
		SyntaxKind.NE, "<>",
		SyntaxKind.GE, ">=",
		SyntaxKind.LE, "<="
	);

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(DISCOURAGED_BOOLEAN_OPERATOR);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		PREFERRED_OPERATORS.keySet().forEach(sk -> context.registerTokenAnalyzer(sk, this::analyzeToken));
	}

	private void analyzeToken(SyntaxToken syntaxToken, IAnalyzeContext context)
	{
		var preferredOperator = PREFERRED_OPERATORS.get(syntaxToken.kind());
		context.report(DISCOURAGED_BOOLEAN_OPERATOR.createFormattedDiagnostic(
			syntaxToken,
			syntaxToken.source(),
			preferredOperator
		));
	}
}
