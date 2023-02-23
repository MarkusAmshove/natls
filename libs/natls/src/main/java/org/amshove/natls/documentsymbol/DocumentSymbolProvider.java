package org.amshove.natls.documentsymbol;

import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natparse.natural.*;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolKind;

import java.util.ArrayList;
import java.util.List;

public class DocumentSymbolProvider
{
	public List<DocumentSymbol> provideSymbols(INaturalModule module)
	{
		var rootSymbol = new DocumentSymbol(module.name(), SymbolKind.Class, getModuleRange(module), LspUtil.toSingleRange(0, 0));
		var rootChildren = new ArrayList<DocumentSymbol>();
		if (module instanceof IHasDefineData hasDefineData && hasDefineData.defineData() != null)
		{
			rootChildren.addAll(createDefineDataSymbols(hasDefineData.defineData()));
		}

		if (module instanceof IModuleWithBody hasBody && hasBody.body() != null)
		{
			rootChildren.addAll(createBodySymbols(hasBody.body()));
		}

		rootSymbol.setChildren(rootChildren);
		return List.of(rootSymbol);
	}

	private Range getModuleRange(INaturalModule module)
	{
		if (module.syntaxTree() == null || module.syntaxTree().descendants().isEmpty())
		{
			return LspUtil.toSingleRange(0, 0);
		}

		var wholeRange = new Range();
		wholeRange.setStart(new Position(0, 0));
		wholeRange.setEnd(LspUtil.toPosition(module.syntaxTree().descendants().last().descendants().last().position()));
		return wholeRange;
	}

	private List<DocumentSymbol> createDefineDataSymbols(IDefineData defineData)
	{
		var defineDataSymbols = new ArrayList<DocumentSymbol>();
		for (var descendant : defineData.descendants())
		{
			if (descendant instanceof IUsingNode using)
			{
				var symbol = new DocumentSymbol();
				symbol.setName(using.target().symbolName());
				symbol.setRange(LspUtil.toRange(using.diagnosticPosition()));
				symbol.setSelectionRange(LspUtil.toRange(using.diagnosticPosition()));
				symbol.setKind(using.isParameterUsing() ? SymbolKind.TypeParameter : SymbolKind.Field);
				defineDataSymbols.add(symbol);
				continue;
			}

			if (descendant instanceof IScopeNode scope)
			{
				for (var variable : scope.variables())
				{
					defineDataSymbols.add(createVariableSymbol(variable));
				}
			}
		}

		return defineDataSymbols;
	}

	private DocumentSymbol createVariableSymbol(IVariableNode variable)
	{
		var symbol = new DocumentSymbol();

		symbol.setName(variable.name());
		symbol.setRange(LspUtil.toRange(variable.declaration()));
		symbol.setSelectionRange(LspUtil.toRange(variable.declaration()));
		symbol.setKind(variable instanceof IGroupNode ? SymbolKind.Struct : SymbolKind.Variable);

		if (variable instanceof IGroupNode groupNode)
		{
			symbol.setKind(SymbolKind.Struct);
			var children = new ArrayList<DocumentSymbol>();
			for (var child : groupNode.variables())
			{
				children.add(createVariableSymbol(child));
			}

			symbol.setChildren(children);
			return symbol;
		}

		if (variable.isArray())
		{
			symbol.setKind(SymbolKind.Array);
		}
		else
		{
			symbol.setKind(SymbolKind.Variable);
		}

		if (variable.scope().isParameter()) // marking parameters is the most important
		{
			symbol.setKind(SymbolKind.TypeParameter);
		}

		return symbol;
	}

	private List<DocumentSymbol> createBodySymbols(IStatementListNode body)
	{
		return body.statements().stream().filter(ISubroutineNode.class::isInstance).flatMap(r ->
		{
			var subroutine = (ISubroutineNode) r;
			var subroutineSymbols = new ArrayList<>(createBodySymbols(subroutine.body()));
			subroutineSymbols.add(new DocumentSymbol(subroutine.declaration().symbolName(), SymbolKind.Method, LspUtil.toRange(subroutine), LspUtil.toRange(subroutine.declaration())));
			return subroutineSymbols.stream();
		})
			.toList();
	}
}
