package org.amshove.natls.viewer;

import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.natural.IInputStatementNode;
import org.amshove.natparse.natural.IModuleWithBody;

public class InputStructureCreator
{
	public InputStructure createStructure(IModuleWithBody module, int inputIndex)
	{
		var inputs = NodeUtil.findNodesOfType(module.body(), IInputStatementNode.class);
		if (inputIndex > inputs.size() - 1)
		{
			return null;
		}

		var structure = new InputStructure();
		structure.setOperands(inputs.get(inputIndex).operands());

		return structure;
	}
}
