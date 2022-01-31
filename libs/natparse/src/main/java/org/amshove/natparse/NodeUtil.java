package org.amshove.natparse;

import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.ISyntaxNode;

public class NodeUtil
{
	private NodeUtil(){}

	public static boolean moduleContainsNode(INaturalModule module, ISyntaxNode node)
	{
		return module.file().getPath().equals(node.position().filePath());
	}
}
