package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.IDataType;
import org.amshove.natparse.natural.ILiteralNode;

class LiteralNode extends TokenNode implements ILiteralNode
{
	private final IDataType dataType;

	public LiteralNode(SyntaxToken token)
	{
		super(token);

		dataType = switch (token.kind())
		{
			case STRING_LITERAL -> new LiteralType(DataFormat.ALPHANUMERIC, token.stringValue().length());
			case NUMBER_LITERAL -> new LiteralType(DataFormat.NUMERIC, getNumericLiteralLength(token.source()));
			case DATE_LITERAL -> new LiteralType(DataFormat.DATE, 4);
			case HEX_LITERAL -> new LiteralType(DataFormat.NUMERIC, token.stringValue().length() * 2.0);
			case TRUE, FALSE -> new LiteralType(DataFormat.LOGIC, 1);
			case ASTERISK -> new LiteralType(DataFormat.NONE, 0);

			default -> throw new IllegalStateException("Invalid literal kind: " + token.kind());
		};
	}

	private double getNumericLiteralLength(String source)
	{
		if (!source.contains(".") && !source.contains(","))
		{
			return source.length();
		}

		var normalized = source.replace(',', '.');
		var split = normalized.split("\\.");
		if (split.length == 1)
		{
			return split[0].length();
		}
		return Double.parseDouble("%s.%s".formatted(split[0].length(), split[1].length())); // there must be a smarter way
	}

	@Override
	public IDataType dataType()
	{
		return dataType;
	}

	record LiteralType(DataFormat format, double length) implements IDataType
	{
		@Override
		public boolean hasDynamicLength()
		{
			return false;
		}
	}
}
