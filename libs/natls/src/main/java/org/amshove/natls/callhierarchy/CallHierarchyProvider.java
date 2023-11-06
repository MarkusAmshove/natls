package org.amshove.natls.callhierarchy;

import org.amshove.natls.SymbolKinds;
import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natls.project.LanguageServerProject;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.natural.IModuleReferencingNode;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.eclipse.lsp4j.*;

import java.util.List;

public class CallHierarchyProvider
{

	private final LanguageServerProject project;

	public CallHierarchyProvider(LanguageServerProject project)
	{
		this.project = project;
	}

	// This only needs to return the top level node
	public List<CallHierarchyItem> prepareCallHierarchy(LanguageServerFile file)
	{
		var item = new CallHierarchyItem();
		item.setRange(new Range(new Position(0, 0), new Position(0, 0)));
		item.setSelectionRange(new Range(new Position(0, 0), new Position(0, 0)));
		item.setName(file.getReferableName());
		item.setDetail(file.getType().toString());
		item.setUri(LspUtil.pathToUri(file.getPath()));
		item.setKind(SymbolKinds.forFileType(file.getReferableName(), file.getType()));
		return List.of(item);
	}

	public List<CallHierarchyIncomingCall> createIncomingCallHierarchyItems(LanguageServerFile file)
	{
		return file.module().callers().stream()
			.map(r ->
			{
				var call = new CallHierarchyIncomingCall();
				call.setFrom(callHierarchyItem(r, project.findFile(r.referencingToken().filePath()).getReferableName(), true));
				call.setFromRanges(List.of(LspUtil.toRange(r)));
				return call;
			})
			.toList();
	}

	public List<CallHierarchyOutgoingCall> createOutgoingCallHierarchyItems(LanguageServerFile callingFile)
	{
		var referencingNodesInCallingFile = NodeUtil.findNodesOfType(callingFile.module().syntaxTree(), IModuleReferencingNode.class);
		return referencingNodesInCallingFile.stream()
			.map(reference ->
			{
				var call = new CallHierarchyOutgoingCall();
				call.setTo(callHierarchyItem(reference, reference.reference().name(), false));
				call.setFromRanges(List.of(LspUtil.toRange(reference)));
				return call;
			})
			.toList();
	}

	private CallHierarchyItem callHierarchyItem(IModuleReferencingNode node, String referableModuleName, boolean isIncomingHierarchyItem)
	{
		var item = new CallHierarchyItem();
		item.setRange(LspUtil.toRange(node.referencingToken()));
		item.setSelectionRange(LspUtil.toRange(node));
		item.setName(referableModuleName);
		var detail = isIncomingHierarchyItem
			? NaturalFileType.fromPath(node.diagnosticPosition().filePath()).toString()
			: node.reference().file().getFiletype().toString();
		item.setDetail(detail);
		item.setUri(node.referencingToken().filePath().toUri().toString());
		var icon = isIncomingHierarchyItem
			? SymbolKinds.forModuleFromNode(node)
			: SymbolKinds.forModule(node.reference());
		item.setKind(icon);
		return item;
	}
}
