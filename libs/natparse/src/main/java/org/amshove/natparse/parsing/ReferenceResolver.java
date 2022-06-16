package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.project.NaturalFileType;

public class ReferenceResolver
{
	public void resolveReferences(NaturalModule naturalModule)
	{
		if(naturalModule.body() != null)
		{
			resolveReferences(naturalModule, naturalModule.body().descendants());
		}
	}

	private void resolveReferences(NaturalModule module, ReadOnlyList<? extends ISyntaxNode> nodes)
	{
		for (var syntaxNode : nodes)
		{
			if(syntaxNode instanceof SymbolReferenceNode unresolvedSymbol
				&& module.file().getFiletype() != NaturalFileType.COPYCODE
				&& unresolvedSymbol.reference() == null // TODO: Remove when this class also references subroutines.
				&& !isExternalSubroutineName(unresolvedSymbol))
			{
				resolveSymbolReference(unresolvedSymbol, module);
			}

			resolveReferences(module, syntaxNode.descendants());
		}
	}

	private boolean isExternalSubroutineName(SymbolReferenceNode unresolvedSymbol)
	{
		return unresolvedSymbol.parent() instanceof ExternalPerformNode externalPerformNode
			&& unresolvedSymbol.referencingToken().equals(externalPerformNode.referencingToken());
	}

	private void resolveSymbolReference(SymbolReferenceNode unresolvedSymbol, NaturalModule module)
	{
		if(module.defineData() == null)
		{
			module.addDiagnostic(ParserErrors.unresolvedReference(unresolvedSymbol));
		}

		var defineData = module.defineData();

		if(unresolvedSymbol.referencingToken().symbolName().startsWith("&")
			|| (unresolvedSymbol.referencingToken().symbolName().contains(".")
			&& unresolvedSymbol.referencingToken().symbolName().split("\\.")[1].startsWith("&")))
		{
			// Copycode parameter
			return;
		}

		if(tryFindAndReference(unresolvedSymbol.token().symbolName(), unresolvedSymbol, defineData))
		{
			return;
		}

		if(unresolvedSymbol.token().symbolName().startsWith("+")
			&& tryFindAndReference(unresolvedSymbol.token().symbolName().substring(1), unresolvedSymbol, defineData))
		{
			// TODO(hack, expressions): This should be handled when parsing expressions.
			return;
		}


		if(unresolvedSymbol.token().symbolName().startsWith("C*")
			&& tryFindAndReference(unresolvedSymbol.token().symbolName().substring(2), unresolvedSymbol, defineData))
		{
			return;
		}

		if(unresolvedSymbol.token().symbolName().startsWith("T*")
			&& tryFindAndReference(unresolvedSymbol.token().symbolName().substring(2), unresolvedSymbol, defineData))
		{
			// TODO(hack, write-statement): This will be obsolete when the WRITE statement is parsed
			return;
		}

		if(unresolvedSymbol.token().symbolName().startsWith("P*")
			&& tryFindAndReference(unresolvedSymbol.token().symbolName().substring(2), unresolvedSymbol, defineData))
		{
			// TODO(hack, write-statement): This will be obsolete when the WRITE statement is parsed
			return;
		}

		if(unresolvedSymbol.token().kind() == SyntaxKind.IDENTIFIER)
		{
			// We don't handle IDENTIFIER_OR_KEYWORD because we can't be sure if it a variable.
			// As long as IDENTIFIER_OR_KEYWORD exists as a SyntaxKind, we only report a diagnostic if we're sure that its meant to be a reference.
			module.addDiagnostic(ParserErrors.unresolvedReference(unresolvedSymbol));
		}
	}

	private boolean tryFindAndReference(String symbolName, ISymbolReferenceNode referenceNode, IDefineData defineData)
	{
		var variable = defineData.findVariable(symbolName);
		if(variable != null)
		{
			variable.addReference(referenceNode);
			return true;
		}

		return defineData.findDdmField(symbolName) != null;
	}
}
