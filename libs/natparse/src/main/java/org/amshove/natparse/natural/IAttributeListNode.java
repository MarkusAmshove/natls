package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface IAttributeListNode extends IOperandNode, ITypeInferable
{
	IDataType DATA_TYPE = new DataType(DataFormat.CONTROL, 2);

	ReadOnlyList<IAttributeNode> attributes();

	@Override
	default IDataType inferType()
	{
		return DATA_TYPE;
	}
}
