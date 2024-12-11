package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.ddm.IDdmField;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IDefineData extends ISyntaxNode
{
	ReadOnlyList<IUsingNode> usings();

	ReadOnlyList<IUsingNode> localUsings();

	ReadOnlyList<IUsingNode> parameterUsings();

	ReadOnlyList<IUsingNode> globalUsings();

	/**
	 * Returns all {@link IParameterDefinitionNode} in order they've been declared.<br/>
	 * This includes `PARAMETER 1...` and `PARAMETER USING ...` in order. <br/>
	 *
	 * Nodes for usings and groups are still included.
	 *
	 * @return Parameter in order
	 */
	ReadOnlyList<IParameterDefinitionNode> declaredParameterInOrder();

	/**
	 * Returns all {@link ITypedVariableNode} in order they've been declared.<br/>
	 * Variables from groups and usings are included, their parents are not. <br/>
	 *
	 * @return All effective parameter in order
	 */
	ReadOnlyList<ITypedVariableNode> effectiveParameterInOrder();

	ReadOnlyList<IVariableNode> variables();

	@Nullable
	IVariableNode findVariable(String symbolName);

	@Nullable
	IDdmField findDdmField(String symbolName);

	@Nullable
	IScopeNode findFirstScopeNode(VariableScope scope);

	@Nullable
	ISyntaxNode findLastScopeNode(VariableScope scope);
}
