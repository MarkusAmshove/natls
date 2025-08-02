package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.IPasswNode;

class PasswNode extends StatementNode implements IPasswNode
{
	private IOperandNode password;

	@Override
	public IOperandNode password()
	{
		return password;
	}

	void setPassword(IOperandNode token)
	{
		password = token;
	}
}
