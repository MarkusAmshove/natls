package org.amshove.natparse.natural;

public sealed interface IMaskOperandNode extends IOperandNode permits IConstantMaskOperandNode, IVariableMaskOperandNode
{
}
