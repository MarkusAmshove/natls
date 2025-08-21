package org.amshove.natls.definition;

import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.*;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.Location;

import java.util.ArrayList;
import java.util.List;

public class DefinitionFinder
{
	public List<Location> findDefinition(DefinitionParams params, LanguageServerFile file)
	{
		try
		{
			return findDefinitions(params, file);
		}
		catch (Exception e)
		{
			return List.of();
		}
	}

	private List<Location> findDefinitions(DefinitionParams params, LanguageServerFile file)
	{
		var invocationPosition = params.getPosition();
		var node = NodeUtil.findTokenNodeAtPosition(
			file.getPath(), invocationPosition.getLine(),
			invocationPosition.getCharacter(), file.module().syntaxTree()
		);

		if (node == null)
		{
			return List.of();
		}

		if (node instanceof IVariableReferenceNode variableReferenceNode)
		{
			return List.of(LspUtil.toLocation(variableReferenceNode.reference()));
		}

		if (node.parent()instanceof ISymbolReferenceNode symbolReferenceNode)
		{
			return List.of(LspUtil.toLocation(symbolReferenceNode.reference()));
		}

		if (node.parent()instanceof IModuleReferencingNode moduleReferencingNode)
		{
			return List.of(LspUtil.toLocation(moduleReferencingNode.reference()));
		}

		if (node.token() != null && node.token().kind().opensStatementWithCloseKeyword())
		{
			return List.of(LspUtil.toLocation(node.parent().descendants().last()));
		}

		if (node.token() != null && node.token().kind().closesStatement())
		{
			return List.of(LspUtil.toLocation(node.parent().descendants().first()));
		}

		if (node.token().kind() == SyntaxKind.LABEL_IDENTIFIER)
		{
			return findStatementLabelDefinition(node.token().symbolName(), file);
		}

		return List.of();
	}

	private List<Location> findStatementLabelDefinition(String labelName, LanguageServerFile file)
	{

		if (!(file.module()instanceof IModuleWithBody withBody))
		{
			return List.of();
		}

		var foundDeclarations = new ArrayList<Location>();
		withBody.acceptStatementVisitor(s ->
		{
			if (!(s instanceof ILabelReferencable referencable))
			{
				return;
			}

			var declaredLabel = referencable.labelIdentifier();
			if (declaredLabel != null && declaredLabel.symbolName().equals(labelName))
			{
				foundDeclarations.add(LspUtil.toLocation(declaredLabel));
			}
		});

		return foundDeclarations;
	}
}
