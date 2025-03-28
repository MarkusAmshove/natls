package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.IDataType;
import org.amshove.natparse.natural.ILiteralNode;

import java.math.BigInteger;

class LiteralNode extends TokenNode implements ILiteralNode
{
	private final IDataType inferredType;
	private static final int LONG_MAX_VALUE_LENGTH = Long.toString(Long.MAX_VALUE).length();

	public LiteralNode(SyntaxToken token)
	{
		super(token);
		inferredType = switch (token.kind())
		{
			case STRING_LITERAL -> new LiteralType(DataFormat.ALPHANUMERIC, token.stringValue().trim().length());
			case NUMBER_LITERAL -> inferNumeric(token);
			case DATE_LITERAL -> new LiteralType(DataFormat.DATE, 4);
			case TIME_LITERAL, EXTENDED_TIME_LITERAL -> new LiteralType(DataFormat.TIME, 7);

			// docs(User-Defined constants): When a hexadecimal constant is transferred to another field, it will be treated as an alphanumeric value (format A).
			case HEX_LITERAL -> new LiteralType(DataFormat.ALPHANUMERIC, token.stringValue().length());

			case TRUE, FALSE -> new LiteralType(DataFormat.LOGIC, 1);
			case ASTERISK -> new LiteralType(DataFormat.NONE, 0);

			default -> throw new IllegalStateException("Invalid literal kind: " + token.kind());
		};
	}

	@Override
	public IDataType inferType()
	{
		return inferredType;
	}

	private IDataType inferNumeric(SyntaxToken token)
	{
		var source = token.source();
		if (!source.contains(".") && !source.contains(","))
		{
			var length = getIntegerLiteralLength(source);
			return length > 4
				? new LiteralType(DataFormat.PACKED, Math.round((source.length() + 1) / 2.0))
				: new LiteralType(DataFormat.INTEGER, length);
		}

		var numericLiteralLength = getNumericLiteralLength(token.source());
		return new LiteralType(DataFormat.PACKED, Math.round((numericLiteralLength + 1) / 2.0));
	}

	private double getNumericLiteralLength(String source)
	{

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
		if (source.contains("E+") || source.contains("E-"))
		{
			// Internally convert Float to Long, then let the rest of existing code take care of the length.
			// Should really use BigInteger, but for now long is fine.
			source = Long.toString((long) Float.parseFloat(source));
		}

		if (source.contains(".") || source.contains(","))
		{
			return getNumericLiteralLength(source);
		}

		if (source.length() >= LONG_MAX_VALUE_LENGTH)
		{
			return getByteSizeBigInteger(source);
		}

		var parsedNumber = Long.parseLong(source);

		if (parsedNumber > Integer.MAX_VALUE || parsedNumber < Integer.MIN_VALUE)
		{
			return 8; // I8 is not a valid type, but will be inferred to NUMERIC instead
		}

		if (parsedNumber >= Byte.MIN_VALUE && parsedNumber <= Byte.MAX_VALUE) // handles the sign better than binaryString
		{
			return 1;
		}

		var byteSize = Long.toBinaryString(parsedNumber).length() / 8.0;
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

	private double getByteSizeBigInteger(String source)
	{
		var bigInt = new BigInteger(source);
		return bigInt.bitLength() / 8.0;
	}

	private IDataType reInferNumericWithoutDecimals(IDataType targetType)
	{
		var digits = Long.toString(Long.parseLong(token().source())).length();
		return new LiteralType(DataFormat.NUMERIC, Math.max(digits, targetType.length()));
	}

	private IDataType reInferInteger(IDataType targetType)
	{
		return new LiteralType(
			DataFormat.INTEGER,
			Math.max(getIntegerLiteralLength(token().source()), targetType.length())
		);
	}

	@Override
	public IDataType reInferType(IDataType targetType)
	{
		if (targetType.format() == DataFormat.ALPHANUMERIC && inferredType.format() == DataFormat.INTEGER)
		{
			return reInferNumericWithoutDecimals(targetType);
		}

		if (inferredType.format() == DataFormat.ALPHANUMERIC && targetType.format() == DataFormat.ALPHANUMERIC
			&& inferredType.length() < targetType.length())
		{
			// Re-interpret type without trimming the literal string value
			return new LiteralType(DataFormat.ALPHANUMERIC, token().stringValue().length());
		}

		if (!targetType.hasSameFamily(inferredType))
		{
			return inferredType;
		}

		if (targetType.format() == DataFormat.NUMERIC && inferredType.format() == DataFormat.INTEGER)
		{
			return reInferNumericWithoutDecimals(targetType);
		}

		if (targetType.format() == DataFormat.INTEGER)
		{
			return reInferInteger(targetType);
		}

		return inferredType;
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
