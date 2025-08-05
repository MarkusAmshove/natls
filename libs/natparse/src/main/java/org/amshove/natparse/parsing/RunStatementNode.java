package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.IRunStatementNode;

import java.util.ArrayList;
import java.util.List;

class RunStatementNode extends StatementNode implements IRunStatementNode
{
	private INaturalModule reference;
	private SyntaxToken referencingToken;
	private final List<IOperandNode> parameter = new ArrayList<>();

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
		return ReadOnlyList.from(parameter);
	}

	void setReference(INaturalModule module)
	{
		this.reference = module;
	}

	void setReferencingToken(SyntaxToken token)
	{
		this.referencingToken = token;
	}

	void addParameter(IOperandNode parameter)
	{
		this.parameter.add(parameter);
	}
}
