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
import org.amshove.natparse.natural.IReadNode;
import org.amshove.natparse.natural.IFindNode;
import org.amshove.natparse.natural.IHistogramNode;
import org.amshove.natparse.natural.IGetSameNode;
import org.amshove.natparse.natural.IGetNode;
import org.amshove.natparse.natural.ISelectNode;
import org.amshove.natparse.natural.IUpdateStatementNode;
import org.amshove.natparse.natural.IDeleteStatementNode;

public class HiddenDBMSAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription HIDDEN_DBMS_IS_DISCOURAGED = DiagnosticDescription.create(
		"NL035",
		"Database statement found in Copycode",
		DiagnosticSeverity.WARNING
	);

	private boolean isDBMSAnalyzerOff;

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(HIDDEN_DBMS_IS_DISCOURAGED);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerNodeAnalyzer(IReadNode.class, this::analyzeDatabaseStatement);
		context.registerNodeAnalyzer(IFindNode.class, this::analyzeDatabaseStatement);
		context.registerNodeAnalyzer(IHistogramNode.class, this::analyzeDatabaseStatement);
		context.registerNodeAnalyzer(IGetSameNode.class, this::analyzeDatabaseStatement);
		context.registerNodeAnalyzer(IGetNode.class, this::analyzeDatabaseStatement);
		context.registerNodeAnalyzer(ISelectNode.class, this::analyzeDatabaseStatement);
		context.registerNodeAnalyzer(IUpdateStatementNode.class, this::analyzeDatabaseStatement);
		context.registerNodeAnalyzer(IDeleteStatementNode.class, this::analyzeDatabaseStatement);
	}

	@Override
	public void beforeAnalyzing(IAnalyzeContext context)
	{
		isDBMSAnalyzerOff = !context.getConfiguration(context.getModule().file(), "natls.style.discourage_hidden_dbms", OPTION_FALSE).equalsIgnoreCase(OPTION_TRUE);
	}

	private void analyzeDatabaseStatement(ISyntaxNode node, IAnalyzeContext context)
	{
		if (isDBMSAnalyzerOff)
		{
			return;
		}

		if (context.getModule().file().getFiletype() != NaturalFileType.COPYCODE)
		{
			return;
		}

		if (!NodeUtil.moduleContainsNode(context.getModule(), node))
		{
			return;
		}

		context.report(HIDDEN_DBMS_IS_DISCOURAGED.createDiagnostic(node));
	}
}
