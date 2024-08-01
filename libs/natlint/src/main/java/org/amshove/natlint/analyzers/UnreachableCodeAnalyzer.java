package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.*;

public class UnreachableCodeAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription UNREACHABLE_CODE = DiagnosticDescription.create(
		"NL026",
		"Unreachable code",
		DiagnosticSeverity.WARNING
	);

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(UNREACHABLE_CODE);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerNodeAnalyzer(IEscapeNode.class, this::analyzeEscape);
	}

	private void analyzeEscape(ISyntaxNode node, IAnalyzeContext context)
	{
		var escape = (IEscapeNode) node;

		if (!(escape.parent()instanceof IStatementListNode parent))
		{
			return;
		}

		var indexOfEscapeInBlock = parent.statements().indexOf(escape);
		var escapeIsLastStatementInBlock = indexOfEscapeInBlock == parent.statements().size() - 1;
		if (escapeIsLastStatementInBlock)
		{
			return;
		}

		var nextStatement = parent.statements().get(indexOfEscapeInBlock + 1);
		if (nextStatement instanceof ISubroutineNode || nextStatement instanceof IOnErrorNode || nextStatement instanceof IEndNode)
		{
			return;
		}

		context.report(UNREACHABLE_CODE.createDiagnostic(nextStatement));
	}
}
