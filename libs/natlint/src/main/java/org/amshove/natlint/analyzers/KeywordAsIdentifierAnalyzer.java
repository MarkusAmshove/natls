package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.ISymbolNode;
import org.amshove.natparse.natural.ISyntaxNode;

public class KeywordAsIdentifierAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription KEYWORD_USED_AS_IDENTIFIER = DiagnosticDescription.create(
		"NL011",
		"Keywords used as identifier are discouraged. Consider prefixing it with a #: %s",
		DiagnosticSeverity.WARNING
	);

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(KEYWORD_USED_AS_IDENTIFIER);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerNodeAnalyzer(ISymbolNode.class, this::analyzeDeclaration);
	}

	private void analyzeDeclaration(ISyntaxNode node, IAnalyzeContext context)
	{
		var symbolNode = ((ISymbolNode) node);
		if (symbolNode.declaration() != null && symbolNode.declaration().originalKind().isPresent())
		{
			context.report((KEYWORD_USED_AS_IDENTIFIER.createFormattedDiagnostic(
				symbolNode.declaration(),
				symbolNode.declaration().source()
			)));
		}
	}
}
