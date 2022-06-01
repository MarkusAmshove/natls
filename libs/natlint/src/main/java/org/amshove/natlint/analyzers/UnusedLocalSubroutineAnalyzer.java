package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.ISubroutineNode;
import org.amshove.natparse.natural.ISyntaxNode;

public class UnusedLocalSubroutineAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription UNUSED_SUBROUTINE = DiagnosticDescription.create(
		"NL005",
		"Subroutine %s is unused",
		DiagnosticSeverity.WARNING
	);

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(UNUSED_SUBROUTINE);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerNodeAnalyzer(ISubroutineNode.class, this::analyzeSubroutine);
	}

	private void analyzeSubroutine(ISyntaxNode node, IAnalyzeContext context)
	{
		var routine = (ISubroutineNode) node;
		if(!routine.declaration().isSameFileAs(routine.diagnosticPosition()))
		{
			// Declared in Copycode
			return;
		}

		if(routine.references().isEmpty())
		{
			context.report(UNUSED_SUBROUTINE.createFormattedDiagnostic(routine.declaration(), routine.declaration().symbolName()));
		}
	}
}
