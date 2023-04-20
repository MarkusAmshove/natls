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
			case NUMBER_LITERAL -> new LiteralType(DataFormat.NUMERIC, Double.parseDouble(token.source().replace(",", ".")));
			case TRUE, FALSE -> new LiteralType(DataFormat.LOGIC, 1);
			case ASTERISK -> new LiteralType(DataFormat.NONE, 0);

			default -> throw new IllegalStateException("Invalid literal kind: " + token.kind());
		};
	}

	@Override
	public IDataType dataType()
	{
		return dataType;
	}

	record LiteralType(DataFormat format, double length) implements IDataType
	{
		@Override
		public DataFormat format()
		{
			return format;
		}

		@Override
		public double length()
		{
			return length;
		}

		@Override
		public boolean hasDynamicLength()
		{
			return false;
		}
	}
}
