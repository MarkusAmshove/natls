package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IIgnoreNode;
import org.amshove.natparse.natural.IStatementListNode;
import org.amshove.natparse.natural.ISyntaxNode;

public class UnnecessaryIgnoreAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription UNNECESSARY_IGNORE = DiagnosticDescription.create(
		"NL024",
		"IGNORE is unnecessary",
		DiagnosticSeverity.INFO
	);

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(UNNECESSARY_IGNORE);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerNodeAnalyzer(IIgnoreNode.class, this::analyzeIgnore);
	}

	private void analyzeIgnore(ISyntaxNode node, IAnalyzeContext context)
	{
		var ignore = (IIgnoreNode) node;

		if (!(NodeUtil.findFirstParentOfType(ignore, IStatementListNode.class)instanceof IStatementListNode parent))
		{
			return;
		}

		if (parent.statements().size() > 1)
		{
			context.report(UNNECESSARY_IGNORE.createDiagnostic(ignore));
		}
	}
}
