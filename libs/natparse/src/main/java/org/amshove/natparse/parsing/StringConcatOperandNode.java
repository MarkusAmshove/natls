package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.*;

import java.util.ArrayList;
import java.util.List;

class StringConcatOperandNode extends BaseSyntaxNode implements IStringConcatOperandNode
{
	private final List<ILiteralNode> literals = new ArrayList<>();
	private String cachedValue;

	@Override
	public ReadOnlyList<ILiteralNode> literals()
	{
		return ReadOnlyList.from(literals);
	}

	@Override
	public String stringValue()
	{
		if (cachedValue != null)
		{
			return cachedValue;
		}

		var builder = new StringBuilder();
		for (var literal : literals)
		{
			builder.append(literal.token().stringValue());
		}
		cachedValue = builder.toString();
		return cachedValue;
	}

	@Override
	public IDataType inferType()
	{
		var stringValue = stringValue();
		return new DataType(DataFormat.ALPHANUMERIC, stringValue.length());
	}

	void addLiteral(ILiteralNode literal)
	{
		literals.add(literal);
	}
}
