package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IExternalPerformNode;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.IOperandNode;

import java.util.ArrayList;
import java.util.List;

final class ExternalPerformNode extends PerformNode implements IExternalPerformNode
{
	private final SyntaxToken referencingToken;
	private INaturalModule reference;
	private List<IOperandNode> providedParameter = new ArrayList<>();

	public ExternalPerformNode(InternalPerformNode internalPerformNode)
	{
		copyFrom(internalPerformNode);
		referencingToken = internalPerformNode.token();
	}

	@Override
	public INaturalModule reference()
	{
		return reference;
	}

	@Override
	public SyntaxToken referencingToken()
	{
		return referencingToken;
	}

	@Override
	public ReadOnlyList<IOperandNode> providedParameter()
	{
		return ReadOnlyList.from(providedParameter);
	}

	void addParameter(IOperandNode parameter)
	{
		providedParameter.add(parameter);
	}

	void setReference(INaturalModule module)
	{
		module.addCaller(this);
		reference = module;
	}
}
