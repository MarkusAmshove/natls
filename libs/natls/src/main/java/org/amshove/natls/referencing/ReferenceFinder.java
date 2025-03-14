package org.amshove.natls.referencing;

import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natls.progress.IProgressMonitor;
import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natls.project.ModuleReferenceCache;
import org.amshove.natls.project.ParseStrategy;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.ReferenceParams;

import java.util.ArrayList;
import java.util.List;

public class ReferenceFinder
{
	public List<Location> findReferences(ReferenceParams params, LanguageServerFile file, IProgressMonitor monitor)
	{
		var position = params.getPosition();

		// Reparse all callers to get parameter and data area references
		file.reparseCallers(monitor);

		var tokenNode = NodeUtil.findTokenNodeAtPosition(file.getPath(), position.getLine(), position.getCharacter(), file.module().syntaxTree());

		var node = NodeUtil.findNodeAtPosition(position.getLine(), position.getCharacter(), file.module());
		if (node instanceof ITokenNode && node.parent() instanceof ISubroutineNode)
		{
			node = node.parent();
		}

		var references = new ArrayList<Location>();

		if (tokenNode instanceof ISymbolReferenceNode symbolReferenceNode)
		{
			references.addAll(resolveReferences(params, symbolReferenceNode.reference()));
		}

		if (node instanceof IReferencableNode referencableNode)
		{
			references.addAll(resolveReferences(params, referencableNode));
		}

		if (node instanceof ISymbolReferenceNode symbolReferenceNode)
		{
			references.addAll(resolveReferences(params, symbolReferenceNode.reference()));
		}

		if (node instanceof IModuleReferencingNode moduleReferencingNode)
		{
			references.addAll(
				moduleReferencingNode.reference().callers().stream()
					.map(caller -> LspUtil.toLocation(caller.referencingToken()))
					.toList()
			);
		}

		var filetype = file.getType();
		if (references.isEmpty() && filetype == NaturalFileType.COPYCODE)
		{
			// We're on the very first node within a copy code, so we're looking for references to the copy code
			for (var callingFile : file.getIncomingReferences())
			{
				if (monitor.isCancellationRequested())
				{
					break;
				}

				var callingModule = callingFile.module(ParseStrategy.WITHOUT_CALLERS);
				if (callingModule instanceof IModuleWithBody withBody)
				{
					var moduleReferencingNodes = NodeUtil.findNodesOfType(withBody.body(), IModuleReferencingNode.class);
					for (var referencingNode : moduleReferencingNodes)
					{
						if (referencingNode.reference().name().equals(file.module().name()))
						{
							references.add(LspUtil.toLocation(referencingNode.referencingToken()));
						}
					}
				}
			}
		}

		if (references.isEmpty())
		{
			var cachedPositions = ModuleReferenceCache.retrieveCachedPositions(file);
			cachedPositions.forEach(p -> references.add(LspUtil.toLocation(p)));

			references.addAll(file.module().callers().stream().map(LspUtil::toLocation).toList());
		}

		return references;
	}

	private List<Location> resolveReferences(ReferenceParams params, IReferencableNode referencableNode)
	{
		var references = new ArrayList<Location>();
		referencableNode.references().stream()
			.map(r -> LspUtil.toLocation(r.referencingToken()))
			.forEach(references::add);

		if (params.getContext().isIncludeDeclaration())
		{
			references.add(LspUtil.toLocation(referencableNode.declaration()));
		}

		return references;
	}
}
