package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxToken;

import java.util.Optional;

public non-sealed interface IConstantMaskOperandNode extends IMaskOperandNode
{
	ReadOnlyList<SyntaxToken> maskContents();
	Optional<IVariableReferenceNode> checkedOperand(); // TODO(type-check): A U - also maskContents() must contain a X for this to be present
}
