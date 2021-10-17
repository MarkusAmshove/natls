package org.amshove.natparse.parsing.ddm;

import org.amshove.natparse.NaturalParseException;
import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.ddm.DescriptorType;
import org.amshove.natparse.natural.ddm.FieldType;
import org.amshove.natparse.natural.ddm.NullValueSuppression;
import org.amshove.natparse.parsing.ddm.text.LinewiseTextScanner;

class FieldParser
{
	private static final int TYPE_INDEX = 0;
	private static final int TYPE_LENGTH = 1;

	private static final int LEVEL_INDEX = 2;
	private static final int LEVEL_LENGTH = 1;

	private static final int SHORTNAME_INDEX = 4;
	private static final int SHORTNAME_LENGTH = 2;

	private static final int NAME_INDEX = 7;
	private static final int NAME_LENGTH = 32;

	private static final int FORMAT_INDEX = 41;
	private static final int FORMAT_LENGTH = 1;

	private static final int LENGTH_INDEX = 43;
	private static final int LENGTH_LENGTH = 4;

	private static final int SUPPRESSION_INDEX = 49;
	private static final int SUPPRESSION_LENGTH = 1;

	private static final int DESCRIPTOR_INDEX = 51;
	private static final int DESCRIPTOR_LENGTH = 1;

	private static final int REMARK_INDEX = 53;

	public DdmField parse(LinewiseTextScanner scanner)
	{
        var fieldLine = scanner.peek();
        var fieldType = parseFieldType(fieldLine);

        var name = parseName(fieldLine);
        var shortname = parseShortname(fieldLine);
        var level = parseLevel(fieldLine);
        var suppression = parseSuppression(fieldLine);
        var descriptorType = parseDescriptorType(fieldLine);
        var remark = parseRemark(fieldLine);

        var length = fieldType == FieldType.GROUP || fieldType == FieldType.PERIODIC
			? 0
			: parseLength(scanner);
        var dataFormat = fieldType == FieldType.GROUP || fieldType == FieldType.PERIODIC
			? DataFormat.NONE
			: parseFormat(fieldLine);

		return new DdmField(
			fieldType,
			level,
			shortname,
			name,
			dataFormat,
			length,
			suppression,
			descriptorType,
			remark);
	}

	private static FieldType parseFieldType(String line)
	{
		return FieldType.fromSource(getField(line, TYPE_INDEX, TYPE_LENGTH));
	}

	private static int parseLevel(String line)
	{
        var level = getField(line, LEVEL_INDEX, LEVEL_LENGTH);
		return Integer.parseInt(level);
	}

	private static String parseShortname(String line)
	{
		return getField(line, SHORTNAME_INDEX, SHORTNAME_LENGTH).trim();
	}

	private static String parseName(String line)
	{
		return getField(line, NAME_INDEX, NAME_LENGTH).trim();
	}

	private static DataFormat parseFormat(String line)
	{
        var format = getField(line, FORMAT_INDEX, FORMAT_LENGTH);
		return DataFormat.fromSource(format);
	}

	protected double parseLength(LinewiseTextScanner scanner)
	{
        var ddmLength = getField(scanner.peek(), LENGTH_INDEX, LENGTH_LENGTH);
		if (ddmLength.contains(","))
		{
			// Using NumberFormat would not throw on invalid Characters
			ddmLength = ddmLength.replace(",", ".");
		}

		try
		{
			return Double.parseDouble(ddmLength);
		}
		catch (NumberFormatException e)
		{
			throw new NaturalParseException(String.format("Can't parse format length \"%s\"", ddmLength));
		}
	}

	private static NullValueSuppression parseSuppression(String line)
	{
        var suppression = getField(line, SUPPRESSION_INDEX, SUPPRESSION_LENGTH);
		return NullValueSuppression.fromSource(suppression);
	}

	private static String getField(String line, int index, int length)
	{
        var endIndex = index + length;

		if (index > line.length())
		{
			return " ";
		}

		// DDM source was saved without trailing whitespace
		if (endIndex > line.length())
		{
            var firstPart = line.substring(index);
			return firstPart + " ".repeat(length - firstPart.length());
		}

		return line.substring(index, index + length);
	}

	private static DescriptorType parseDescriptorType(String line)
	{
		return DescriptorType.fromSource(getField(line, DESCRIPTOR_INDEX, DESCRIPTOR_LENGTH));
	}

	private static String parseRemark(String line)
	{
		if (line.length() < REMARK_INDEX)
		{
			return "";
		}

		return line.substring(REMARK_INDEX).trim();
	}
}
