package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.ICharacterRepetitionOperandNode;
import org.amshove.natparse.natural.ILiteralNode;
import org.amshove.natparse.natural.IOperandNode;

class CharacterRepetitionOperandNode extends OutputOperandNode implements ICharacterRepetitionOperandNode
{
	private int repetition;

	CharacterRepetitionOperandNode(OutputOperandNode inputOperand)
	{
		copyFrom(inputOperand);
		setOperand(inputOperand.operand());
		// Attributes get parsed after character repetition
	}

	@Override
	public int repetition()
	{
		return this.repetition;
	}

	void setRepetition(IOperandNode literal)
	{
		this.repetition = ((ILiteralNode) literal).token().intValue();
	}
}
