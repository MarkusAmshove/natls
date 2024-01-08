package org.amshove.natls.viewer;

import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.natural.IInputStatementNode;
import org.amshove.natparse.natural.IModuleWithBody;

public class InputStructureCreator
{
	public InputStructure createStructure(IModuleWithBody module, int inputPosition)
	{
		var inputs = NodeUtil.findFirstStatementOfType(IInputStatementNode.class, module.body());

		var structure = new InputStructure();
		structure.setOperands(inputs.operands());

		return structure;
	}
}
