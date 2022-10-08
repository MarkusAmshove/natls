package org.amshove.natparse.natural;

import java.util.Optional;

// TODO: (type-checking) check operand for typ(A, B, U)
public interface ISubstringOperandNode extends IOperandNode
{
	IOperandNode operand();
	Optional<IOperandNode> startPosition();
	Optional<IOperandNode> length();
}
