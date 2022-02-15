package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.IPosition;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IExternalPerformNode;
import org.amshove.natparse.natural.IInternalPerformNode;
import org.amshove.natparse.natural.ISyntaxNode;

public class SubroutineNameMismatchAnalyzer extends AbstractAnalyzer
{
	public static DiagnosticDescription SUBROUTINE_NAME_MISMATCH = DiagnosticDescription.create(
			"NL004",
			"""
			Subroutine name cut on compilation
			Although the subroutine name from this PERFORM and the DEFINE match at runtime (because they're cut to 32 characters),
			it is confusing that the PERFORM and DEFINE names differ within the source code.

			Names (pipe shows shows the cut at runtime):
			    PERFORM name: %s
			    DEFINE  name: %s
			                  --------------------------------^
			""",
			DiagnosticSeverity.WARNING
	);

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(SUBROUTINE_NAME_MISMATCH);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerNodeAnalyzer(IInternalPerformNode.class, this::analyzeInternalPerform);
		context.registerNodeAnalyzer(IExternalPerformNode.class, this::analyzeExternalPerform);
	}

	private void analyzeInternalPerform(ISyntaxNode syntaxNode, IAnalyzeContext context)
	{
		var performNode = (IInternalPerformNode)syntaxNode;

		if(performNode.reference() == null)
		{
			return;
		}

		analyze(performNode.referencingToken(), performNode.referencingToken().symbolName(), performNode.reference().declaration().symbolName(), context);
	}

	private void analyzeExternalPerform(ISyntaxNode syntaxNode, IAnalyzeContext context)
	{
		var performNode = (IExternalPerformNode)syntaxNode;

		if(performNode.reference() == null)
		{
			return;
		}

		analyze(performNode.referencingToken(), performNode.referencingToken().symbolName(), performNode.reference().file().getReferableName(), context);
	}

	private void analyze(IPosition reportPosition, String callerName, String definitionName, IAnalyzeContext context)
	{
		if(!callerName.equals(definitionName))
		{
			context.report(SUBROUTINE_NAME_MISMATCH.createFormattedDiagnostic(
					reportPosition,
					callerName.substring(0, 32) + "|" + callerName.substring(32),
					definitionName.substring(0, 32) + "|" + definitionName.substring(32)
				)
			);
		}
	}
}
