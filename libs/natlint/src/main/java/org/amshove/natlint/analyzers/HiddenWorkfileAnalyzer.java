package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.amshove.natparse.natural.IReadWorkNode;
import org.amshove.natparse.natural.IWriteWorkNode;
import org.amshove.natparse.natural.ICloseWorkNode;
import org.amshove.natparse.natural.IDefineWorkFileNode;

public class HiddenWorkfileAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription HIDDEN_WORKFILE_STATEMENT_IS_DISCOURAGED = DiagnosticDescription.create(
		"NL033",
		"Use of [DEFINE|READ|WRITE|CLOSE] WORK FILE statement outside Program is discouraged.",
		DiagnosticSeverity.WARNING
	);

	private boolean isWorkfileAnalyzerOff;

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(HIDDEN_WORKFILE_STATEMENT_IS_DISCOURAGED);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerNodeAnalyzer(IDefineWorkFileNode.class, this::analyzeTransaction);
		context.registerNodeAnalyzer(IReadWorkNode.class, this::analyzeTransaction);
		context.registerNodeAnalyzer(IWriteWorkNode.class, this::analyzeTransaction);
		context.registerNodeAnalyzer(ICloseWorkNode.class, this::analyzeTransaction);
	}

	@Override
	public void beforeAnalyzing(IAnalyzeContext context)
	{
		isWorkfileAnalyzerOff = !context.getConfiguration(context.getModule().file(), "natls.style.discourage_hiddenworkfiles", OPTION_FALSE).equalsIgnoreCase(OPTION_TRUE);
	}

	private void analyzeTransaction(ISyntaxNode node, IAnalyzeContext context)
	{
		if (isWorkfileAnalyzerOff)
		{
			return;
		}

		if (context.getModule().file().getFiletype() == NaturalFileType.PROGRAM)
		{
			return;
		}

		if (!NodeUtil.moduleContainsNode(context.getModule(), node))
		{
			return;
		}

		context.report(HIDDEN_WORKFILE_STATEMENT_IS_DISCOURAGED.createDiagnostic(node));
	}
}
