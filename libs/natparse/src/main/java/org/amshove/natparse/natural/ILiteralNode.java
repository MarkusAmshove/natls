package org.amshove.natparse.natural;

public interface ILiteralNode extends ITokenNode, IOperandNode, ITypeInferable
{
	/**
	 * This method handles different implicit conversions based on the target type.<br/>
	 * For example, `23` is inferred as I1 but should be interpreted as N2 when compared to NUMERIC.<br/>
	 * Alphanumeric types get their size adjusted to un-trimmed size.
	 *
	 * @return the interpreted type based on the target type
	 */
	IDataType reInferType(IDataType targetType);
}
