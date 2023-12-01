package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IAssignmentStatementNode;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.IResetStatementNode;
import org.amshove.natparse.natural.IStatementListNode;

abstract class AbstractOperandParsingTest extends AbstractParserTest<IStatementListNode>
{
	protected AbstractOperandParsingTest()
	{
		super(StatementListParser::new);
	}

	protected IOperandNode parseOperand(String source)
	{
		var statement = assertParsesWithoutDiagnostics("#I := %s".formatted(source)).statements().first();
		return assertNodeType(statement, IAssignmentStatementNode.class).operand();
	}

	protected ReadOnlyList<IOperandNode> parseOperands(String source)
	{
		var statement = assertParsesWithoutDiagnostics("RESET %s".formatted(source)).statements().first();
		return assertNodeType(statement, IResetStatementNode.class).operands();
	}
}
