package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;

public interface IMoveStatementNode extends IStatementNode, IMutateVariables
{
	ReadOnlyList<IOperandNode> targets();

	IOperandNode operand();

	SyntaxKind moveKind();

	SyntaxKind byKind();

	SyntaxKind direction();

	boolean isRounded();

	boolean isEdited();

	boolean isNormalized();

	boolean isEncoded();

	boolean isAll();
}
