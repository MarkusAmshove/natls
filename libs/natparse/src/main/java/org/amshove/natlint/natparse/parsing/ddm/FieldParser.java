package org.amshove.natlint.natparse.parsing.ddm;

import org.amshove.natlint.natparse.NaturalParseException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class FieldParser
{
	private static final int TYPE_INDEX = 0;
	private static final int TYPE_LENGTH = 1;

	/*
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
	private static final int SUPPRESION_LENGTH = 1;

	private static final int DESCRIPTOR_INDEX = 51;
	private static final int DESCRIPTOR_LENGTH = 1;

	private static final int REMARK_INDEX = 53;
	*/

	public DdmField parse(String fieldLine)
	{
		return new DdmField(parseFieldType(fieldLine));
	}

	private static FieldType parseFieldType(String line)
	{
		String type = line.substring(TYPE_INDEX, TYPE_INDEX + TYPE_LENGTH);
		if(type.equals(" "))
		{
			return FieldType.NONE;
		}
		if(type.equals("M"))
		{
			return FieldType.MULTIPLE;
		}
		if(type.equals("G"))
		{
			return FieldType.GROUP;
		}
		if(type.equals("P"))
		{
			return FieldType.PERIODIC;
		}

		throw new NaturalParseException(String.format("Can't determine DDM FieldType from \"%s\"", type));
	}
}
