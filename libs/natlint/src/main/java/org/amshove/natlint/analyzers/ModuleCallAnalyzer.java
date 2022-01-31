package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.ICallnatNode;
import org.amshove.natparse.natural.IFetchNode;
import org.amshove.natparse.natural.ISyntaxNode;

public class ModuleCallAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription CALLED_MODULE_NAME_TRAILING_WHITESPACE = DiagnosticDescription.create(
		"NL003",
		"Module name contains trailing or leading whitespace",
		DiagnosticSeverity.WARNING
	);

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(CALLED_MODULE_NAME_TRAILING_WHITESPACE);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerNodeAnalyzer(ICallnatNode.class, this::analyzeCallnat);
		context.registerNodeAnalyzer(IFetchNode.class, this::analyzeFetch);
	}

	private void analyzeFetch(ISyntaxNode syntaxNode, IAnalyzeContext context)
	{
		checkModuleName(((IFetchNode) syntaxNode).referencingToken(), context);
	}

	private void analyzeCallnat(ISyntaxNode syntaxNode, IAnalyzeContext context)
	{
		checkModuleName(((ICallnatNode) syntaxNode).referencingToken(), context);
	}

	private static void checkModuleName(SyntaxToken moduleName, IAnalyzeContext context)
	{
		if(moduleName.stringValue().length() != moduleName.stringValue().trim().length())
		{
			context.report(CALLED_MODULE_NAME_TRAILING_WHITESPACE.createDiagnostic(moduleName));
		}
	}
}
