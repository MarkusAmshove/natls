package org.amshove.natparse.parsing;

import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.project.NaturalFileType;

public class ReferenceResolver
{
	private final IModuleProvider moduleProvider;

	public ReferenceResolver(IModuleProvider moduleProvider)
	{
		this.moduleProvider = moduleProvider;
	}

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
		if(unresolvedSymbol.parent() instanceof IInternalPerformNode internalPerform)
		{
			resolvePerform(unresolvedSymbol, module, (InternalPerformNode) internalPerform);
			return;
		}

		if(module.defineData() == null)
		{
			module.addDiagnostic(ParserErrors.unresolvedReference(unresolvedSymbol));
		}

		if(unresolvedSymbol.referencingToken().symbolName().startsWith("&")
			|| (unresolvedSymbol.referencingToken().symbolName().contains(".")
			&& unresolvedSymbol.referencingToken().symbolName().split("\\.")[1].startsWith("&")))
		{
			// Copycode parameter
			return;
		}

		if(tryFindAndReference(unresolvedSymbol.token().symbolName(), unresolvedSymbol, module))
		{
			return;
		}

		if(unresolvedSymbol.token().symbolName().startsWith("+")
			&& tryFindAndReference(unresolvedSymbol.token().symbolName().substring(1), unresolvedSymbol, module))
		{
			// TODO(hack, expressions): This should be handled when parsing expressions.
			return;
		}


		if(unresolvedSymbol.token().symbolName().startsWith("C*")
			&& tryFindAndReference(unresolvedSymbol.token().symbolName().substring(2), unresolvedSymbol, module))
		{
			return;
		}

		if(unresolvedSymbol.token().symbolName().startsWith("T*")
			&& tryFindAndReference(unresolvedSymbol.token().symbolName().substring(2), unresolvedSymbol, module))
		{
			// TODO(hack, write-statement): This will be obsolete when the WRITE statement is parsed
			return;
		}

		if(unresolvedSymbol.token().symbolName().startsWith("P*")
			&& tryFindAndReference(unresolvedSymbol.token().symbolName().substring(2), unresolvedSymbol, module))
		{
			// TODO(hack, write-statement): This will be obsolete when the WRITE statement is parsed
			return;
		}

		module.addDiagnostic(ParserErrors.unresolvedReference(unresolvedSymbol));
	}

	private void resolvePerform(SymbolReferenceNode unresolvedSymbol, NaturalModule module, InternalPerformNode internalPerform)
	{
		var declaredSubroutines = NodeUtil.findStatementsOfType(module.body(), ISubroutineNode.class);
		for (var declaredSubroutine : declaredSubroutines)
		{
			if(declaredSubroutine.declaration().trimmedSymbolName(32).equals(unresolvedSymbol.referencingToken().trimmedSymbolName(32)))
			{
				declaredSubroutine.addReference(internalPerform);
				return;
			}
		}

		if(module.isTestCase()
			&& (internalPerform.referencingToken().symbolName().equals("TEARDOWN")
				|| internalPerform.referencingToken().symbolName().equals("SETUP")))
		{
			// Skip these unresolved subroutines.
			// These are special cases for NatUnit, because it doesn't force you to implement them.
			// It however calls them if they're present.
			return;
		}

		var externalModule = moduleProvider.findNaturalModule(unresolvedSymbol.referencingToken().trimmedSymbolName(32));
		if(externalModule != null)
		{
			var externalPerform = new ExternalPerformNode(internalPerform);
			((BaseSyntaxNode) internalPerform.parent()).replaceChild(internalPerform, externalPerform);
			externalPerform.setReference(externalModule);
			return;
		}
		module.addDiagnostic(ParserErrors.unresolvedReference(unresolvedSymbol));
	}

	private boolean tryFindAndReference(String symbolName, ISymbolReferenceNode referenceNode, NaturalModule module)
	{
		var defineData = ((DefineDataNode)((IHasDefineData)module).defineData());
		var foundVariables = defineData.findVariablesWithName(symbolName);

		if(foundVariables.size() > 1)
		{
			var possibleQualifications = new StringBuilder();
			for (var foundVariable : foundVariables)
			{
				possibleQualifications.append(foundVariable.qualifiedName()).append(" ");
			}

			if(defineData.findDdmField(symbolName) != null) // TODO(read-statement): Currently only necessary here because we don't parse FIND and READ yet
			{
				return true;
			}

			module.addDiagnostic(ParserErrors.ambiguousSymbolReference(referenceNode, possibleQualifications.toString()));
		}


		if(!foundVariables.isEmpty())
		{
			foundVariables.get(0).addReference(referenceNode);
			return true;
		}

		return defineData.findDdmField(symbolName) != null;
	}
}
