package org.amshove.natparse.parsing;

import org.amshove.natparse.IPosition;
import org.amshove.natparse.natural.IIncludeNode;
import org.amshove.natparse.natural.IStatementListNode;
import org.amshove.natparse.natural.ITokenNode;

class IncludeNode extends ModuleReferencingNode implements IIncludeNode
{
	private IStatementListNode body;

	@Override
	public IStatementListNode body()
	{
		return body;
	}

	void setBody(IStatementListNode body, IPosition diagnosticPosition)
	{
		addNode((StatementListNode) body);
		this.body = body;
		body.directDescendantsOfType(ITokenNode.class).forEach(tokenNode ->
		{
			tokenNode.token().setDiagnosticPosition(diagnosticPosition);
		});
	}
}
