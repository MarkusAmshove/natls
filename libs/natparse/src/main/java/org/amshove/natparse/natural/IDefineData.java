package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.ddm.IDdmField;

import javax.annotation.Nullable;

public interface IDefineData extends ISyntaxNode
{
	ReadOnlyList<IUsingNode> usings();
	ReadOnlyList<IUsingNode> localUsings();
	ReadOnlyList<IUsingNode> parameterUsings();
	ReadOnlyList<IUsingNode> globalUsings();

	/**
	 * Returns all {@link IParameterDefinitionNode} in order they've been declared.</br>
	 * This includes `PARAMETER 1...` and `PARAMETER USING ...` in order. </br>
	 *
	 * `USING`s are not "exploded", which means the variables from within the `USING` are not included.
	 *
	 * @return Non-expanded parameter in order
	 */
	ReadOnlyList<IParameterDefinitionNode> parameterInOrder();

	ReadOnlyList<IVariableNode> variables();

	@Nullable IVariableNode findVariable(String symbolName);
	@Nullable IDdmField findDdmField(String symbolName);
}
