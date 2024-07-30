package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IModuleWithBody;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.ISubroutineNode;

public class BetweenSubroutinesAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription DISCOURAGED_CODE_BETWEEN_SUBROUTINES = DiagnosticDescription.create(
		"NL029",
		"Code in between subroutines is strongly discouraged",
		DiagnosticSeverity.ERROR
	);

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(DISCOURAGED_CODE_BETWEEN_SUBROUTINES);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerModuleAnalyzer(this::analyzeModule);
	}

	private void analyzeModule(INaturalModule module, IAnalyzeContext context)
	{
		if (!module.file().getFiletype().canHaveBody())
		{
			return;
		}

		if (module instanceof IModuleWithBody hasBody && hasBody.body() == null)
		{
			return;
		}

		var moduleWithBody = (IModuleWithBody) module;
		var body = moduleWithBody.body();

		ISyntaxNode firstNode = null;
		ISyntaxNode lastNode = null;
		for (ISyntaxNode node : body)
		{
			if (node instanceof ISubroutineNode)
			{
				if (firstNode == null)
					firstNode = node;
				lastNode = node;
			}
		}

		if (firstNode == null || firstNode == lastNode)
		{
			return;
		}

		var analyse = false;
		for (ISyntaxNode node : body)
		{
			if (node == firstNode)
			{
				analyse = true;
				continue;
			}

			if (node == lastNode)
			{
				return;
			}

			if (analyse && !(node instanceof ISubroutineNode))
			{
				context.report(DISCOURAGED_CODE_BETWEEN_SUBROUTINES.createDiagnostic(node));
				return;
			}
		}
	}
}
