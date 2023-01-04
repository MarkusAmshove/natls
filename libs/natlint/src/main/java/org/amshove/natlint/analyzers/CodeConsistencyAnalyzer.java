package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;

public class CodeConsistencyAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription PREFER_DIFFERENT_TOKEN = DiagnosticDescription.create(
		"NL010",
		"Prefer %s over %s for consistency",
		DiagnosticSeverity.INFO
	);

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(PREFER_DIFFERENT_TOKEN);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		registerTokenPreference(context, SyntaxKind.OCCURRENCE, SyntaxKind.OCC);
	}

	private void registerTokenPreference(ILinterContext context, SyntaxKind unwantedToken, SyntaxKind preferredToken)
	{
		context.registerTokenAnalyzer(
			unwantedToken, (token, analyzerContext) -> analyzerContext.report(
				PREFER_DIFFERENT_TOKEN.createFormattedDiagnostic(
					token,
					preferredToken.toString(),
					unwantedToken.toString()
				)
			)
		);
	}
}
