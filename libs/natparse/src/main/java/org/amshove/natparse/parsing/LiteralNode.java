package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.IDataType;
import org.amshove.natparse.natural.ILiteralNode;

class LiteralNode extends TokenNode implements ILiteralNode
{
	public LiteralNode(SyntaxToken token)
	{
		super(token);
	}

	@Override
	public IDataType inferType(DataFormat targetFormat)
	{
		var token = token();
		return switch (token.kind())
		{
			case STRING_LITERAL -> new LiteralType(DataFormat.ALPHANUMERIC, token.stringValue().length());
			case NUMBER_LITERAL -> switch (targetFormat)
				{
					case BINARY -> new LiteralType(DataFormat.BINARY, getIntegerLiteralLength(token.source()));
					case INTEGER -> new LiteralType(DataFormat.INTEGER, getIntegerLiteralLength(token.source()));
					case FLOAT -> new LiteralType(DataFormat.FLOAT, getIntegerLiteralLength(token.source()));
					case PACKED -> new LiteralType(DataFormat.PACKED, getNumericLiteralLength(token.source()));
					default -> new LiteralType(DataFormat.NUMERIC, getNumericLiteralLength(token.source()));
				};
			case DATE_LITERAL -> new LiteralType(DataFormat.DATE, 4);
			case TIME_LITERAL, EXTENDED_TIME_LITERAL -> new LiteralType(DataFormat.TIME, 7);

			// docs(User-Defined constants): When a hexadecimal constant is transferred to another field, it will be treated as an alphanumeric value (format A).
			case HEX_LITERAL -> new LiteralType(DataFormat.ALPHANUMERIC, token.stringValue().length());

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

	private double getIntegerLiteralLength(String source)
	{
		if (source.contains(".") || source.contains(","))
		{
			return getNumericLiteralLength(source);
		}

		var byteSize = Long.toBinaryString(Long.parseLong(source)).length() / 8.0;
		if (byteSize < 1)
		{
			return 1;
		}

		if (byteSize < 2)
		{
			return 2;
		}

		if (byteSize > 4)
		{
			// for too big literals, round the bytes up.
			// I5 isn't a valid type, but will result in the correct type error.
			return (int) (byteSize + 1);
		}

		return 4;
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
