package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.ICallFileNode;
import org.amshove.natparse.natural.ILiteralNode;
import org.amshove.natparse.natural.IOperandNode;

class CallFileNode extends StatementWithBodyNode implements ICallFileNode
{
	private ILiteralNode calling;
	private IOperandNode controlField;
	private IOperandNode recordArea;

	@Override
	public ILiteralNode calling()
	{
		return this.calling;
	}

	@Override
	public IOperandNode controlField()
	{
		return this.controlField;
	}

	@Override
	public IOperandNode recordArea()
	{
		return this.recordArea;
	}

	void setControlField(IOperandNode controlField)
	{
		this.controlField = controlField;
	}

	void setRecordArea(IOperandNode recordArea)
	{
		this.recordArea = recordArea;
	}

	void setCalling(ILiteralNode calling)
	{
		this.calling = calling;
	}
}
