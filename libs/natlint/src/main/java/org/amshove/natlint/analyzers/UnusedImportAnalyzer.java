package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.IUsingNode;

public class UnusedImportAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription UNUSED_IMPORT = DiagnosticDescription.create(
		"NL002",
		"Using %s is unused",
		DiagnosticSeverity.WARNING
	);

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(UNUSED_IMPORT);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerNodeAnalyzer(IUsingNode.class, this::analyzeUsing);
	}

	private void analyzeUsing(ISyntaxNode node, IAnalyzeContext context)
	{
		var using = (IUsingNode) node;
		if (using.defineData() == null)
		{
			return;
		}
		if (using.defineData().variables().stream().flatMap(v -> v.references().stream()).noneMatch(r -> NodeUtil.moduleContainsNodeByDiagnosticPosition(context.getModule(), r)))
		{
			context.report(UNUSED_IMPORT.createFormattedDiagnostic(using.target(), using.target().symbolName()));
		}
	}
}
