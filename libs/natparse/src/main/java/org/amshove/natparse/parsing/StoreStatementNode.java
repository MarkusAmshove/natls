package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.IStoreStatementNode;
import org.amshove.natparse.natural.IVariableReferenceNode;
import org.jspecify.annotations.Nullable;

class StoreStatementNode extends StatementNode implements IStoreStatementNode
{
	private IVariableReferenceNode viewReference;
	private IOperandNode password;
	private IOperandNode cipher;

	@Override
	public IVariableReferenceNode view()
	{
		return viewReference;
	}

	void setViewReference(IVariableReferenceNode viewReference)
	{
		this.viewReference = viewReference;
	}

	@Override
	public @Nullable IOperandNode password()
	{
		return password;
	}

	@Override
	public @Nullable IOperandNode cipher()
	{
		return cipher;
	}

	void setPassword(IOperandNode password)
	{
		this.password = password;
	}

	void setCipher(IOperandNode cipher)
	{
		this.cipher = cipher;
	}
}
