package org.amshove.natparse;

import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.ISyntaxTree;

public class NodeUtil
{
	private NodeUtil(){}

	public static boolean moduleContainsNode(INaturalModule module, ISyntaxNode node)
	{
		return module.file().getPath().equals(node.position().filePath());
	}

	public static ISyntaxNode findNodeAtPosition(int line, int character, INaturalModule module)
	{
		return findNodeAtPosition(line, character, module.syntaxTree());
	}

	public static ISyntaxNode findNodeAtPosition(int line, int character, ISyntaxTree syntaxTree)
	{
		ISyntaxNode previousNode = null;

		for (var node : syntaxTree)
		{
			if(node.position().line() == line && node.position().offsetInLine() == character)
			{
				return node;
			}

			if(node.position().line() == line && node.position().offsetInLine() > character)
			{
				return previousNode;
			}

			if(node.position().line() > line)
			{
				return findNodeAtPosition(line, character, previousNode);
			}

			previousNode = node;
		}

		return null;
	}
}
