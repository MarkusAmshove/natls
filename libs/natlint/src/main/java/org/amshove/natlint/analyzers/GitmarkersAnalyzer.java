package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.project.NaturalFileType;

public class GitmarkersAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription DISCOURAGED_GITMARKERS_IN_COMMENT = DiagnosticDescription.create(
		"NL030",
		"Gitmarkers in comments can be confusing, remove any '<<' or '>>'",
		DiagnosticSeverity.WARNING
	);

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(DISCOURAGED_GITMARKERS_IN_COMMENT);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerModuleAnalyzer(this::analyzeModule);
	}

	private void analyzeModule(INaturalModule module, IAnalyzeContext context)
	{
		if (module.file().getFiletype().equals(NaturalFileType.MAP)) /* Maps are unpredictable */
		{
			return;
		}

		for (SyntaxToken comment : module.comments())
		{
			if (comment.source().contains("<<") || comment.source().contains(">>"))
			{
				context.report(DISCOURAGED_GITMARKERS_IN_COMMENT.createDiagnostic(comment));
				return;
			}
		}
	}
}
