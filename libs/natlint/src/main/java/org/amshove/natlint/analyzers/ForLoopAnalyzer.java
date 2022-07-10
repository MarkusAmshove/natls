package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.IForLoopNode;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.ISystemFunctionNode;

public class ForLoopAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription UPPER_BOUND_OCC = DiagnosticDescription.create(
		"NL009",
		"Upper bound of FOR-loop should not be *OCC",
		DiagnosticSeverity.WARNING
	);

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(UPPER_BOUND_OCC);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerNodeAnalyzer(IForLoopNode.class, this::analyzeFor);
	}

	private void analyzeFor(ISyntaxNode iSyntaxNode, IAnalyzeContext context)
	{
		var forLoop = (IForLoopNode) iSyntaxNode;

		var upperBound = forLoop.upperBound();
		if(upperBound == null)
		{
			return;
		}

		if(!(upperBound instanceof ISystemFunctionNode sysFuncNode))
		{
			return;
		}

		if(sysFuncNode.systemFunction() == SyntaxKind.OCC || sysFuncNode.systemFunction() == SyntaxKind.OCCURENCE)
		{
			context.report(UPPER_BOUND_OCC.createDiagnostic(sysFuncNode.position()));
		}
	}
}
