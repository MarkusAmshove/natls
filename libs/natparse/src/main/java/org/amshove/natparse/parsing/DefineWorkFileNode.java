package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IDefineWorkFileNode;
import org.amshove.natparse.natural.ILiteralNode;
import org.amshove.natparse.natural.IOperandNode;

class DefineWorkFileNode extends StatementNode implements IDefineWorkFileNode
{
	private ILiteralNode number;
	private IOperandNode path;
	private IOperandNode type;
	private IOperandNode attributes;

	@Override
	public ILiteralNode number()
	{
		return number;
	}

	@Override
	public IOperandNode path()
	{
		return path;
	}

	@Override
	public IOperandNode type()
	{
		return type;
	}

	@Override
	public IOperandNode attributes()
	{
		return attributes;
	}

	void setNumber(ILiteralNode number)
	{
		this.number = number;
	}

	void setPath(IOperandNode path)
	{
		this.path = path;
	}

	void setType(IOperandNode type)
	{
		this.type = type;
	}

	void setAttributes(IOperandNode attributes)
	{
		this.attributes = attributes;
	}
}
