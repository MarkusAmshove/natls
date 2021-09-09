package org.amshove.natlint.natparse.parsing.ddm;

import com.google.common.collect.Lists;
import org.amshove.natlint.natparse.NaturalParseException;
import org.amshove.natlint.natparse.natural.ddm.DescriptorType;
import org.amshove.natlint.natparse.natural.ddm.FieldType;
import org.amshove.natlint.natparse.parsing.ddm.text.LinewiseTextScanner;

import java.util.List;
import java.util.regex.Pattern;

public class DdmParser
{
	private static final String PREDIT_COMMENT_START = "*";

	private final DdmMetadataParser metadataParser = new DdmMetadataParser();
	private final FieldParser fieldParser = new FieldParser();
	private final SuperdescriptorChildParser superdescriptorChildParser = new SuperdescriptorChildParser();

	private static final List<String> linesToSkip = Lists.newArrayList(
		"DDM OUTPUT TERMINTAED",
		"SOURCE FIELD(S)",
		"- - -- ---------------",
		"T L DB Name",
		"Natural Source Header",
		"TYPE: ",
		":CP");

	private DataDefinitionModule ddm;

	public DataDefinitionModule parseDdm(String content)
	{
		resetParser();
		String[] lines = content.split("[\\r\\n]+");
		LinewiseTextScanner scanner = new LinewiseTextScanner(lines);

		while (!scanner.isAtEnd())
		{
			String line = scanner.peek();

			if (isLineToSkip(line))
			{
				scanner.advance();
				continue;
			}

			if (line.startsWith("DB:"))
			{
				DdmMetadata metadata = metadataParser.parseMetadataLine(line);
				ddm = new DataDefinitionModule(metadata.databaseNumber(), metadata.fileNumber(), metadata.name(), metadata.defaultSequence());
				scanner.advance();
				continue;
			}

			DdmField field = parseField(line, scanner.currentLineNumber());
			if (field.fieldType() == FieldType.GROUP)
			{
				GroupField groupField = new GroupField(field);
				scanner.advance();
				parseGroup(scanner, groupField);
				ddm.addField(groupField);
				continue;
			}

			if (field.descriptor() == DescriptorType.SUPERDESCRIPTOR)
			{
				field = parseSuperdescriptor(scanner, new Superdescriptor(field));
				ddm.addField(field);
				continue;
			}

			ddm.addField(field);
			scanner.advance();
		}

		return ddm;
	}

	private DdmField parseField(String line, int linenumber)
	{
		try
		{
			return fieldParser.parse(line);
		}
		catch (Exception e)
		{
			throw new NaturalParseException(e, linenumber);
		}
	}

	private void parseGroup(LinewiseTextScanner scanner, GroupField currentField)
	{
		while (!scanner.isAtEnd())
		{
			if (isLineToSkip(scanner.peek()))
			{
				return;
			}

			DdmField nextField = parseField(scanner.peek(), scanner.currentLineNumber());

			if (nextField.level() <= currentField.level())
			{
				break;
			}

			if (nextField.fieldType() != FieldType.GROUP)
			{
				currentField.addChildField(nextField);
				scanner.advance();
			}
			else
			{
				GroupField childGroupField = new GroupField(nextField);
				scanner.advance();
				parseGroup(scanner, childGroupField);
				currentField.addChildField(childGroupField);
			}
		}
	}

	private Superdescriptor parseSuperdescriptor(LinewiseTextScanner scanner, DdmField field)
	{
		scanner.advance();
		// SOURCE FIELD(S) comment from predic
		scanner.advance();

		Superdescriptor superdescriptor = new Superdescriptor(field);

		while (!scanner.isAtEnd() && containsSuperdescriptorSourceFieldRange(scanner.peek()))
		{
			SuperdescriptorChild child = superdescriptorChildParser.parse(scanner.peek());
			superdescriptor.addChildField(child);
			scanner.advance();
		}

		return superdescriptor;
	}

	private static final Pattern SUPERDESCRIPTOR_CHILD_RANGE_PATTERN = Pattern.compile("^.*\\(\\d+-\\d+\\).*$");

	private static boolean containsSuperdescriptorSourceFieldRange(String line)
	{
		return SUPERDESCRIPTOR_CHILD_RANGE_PATTERN.matcher(line).matches();
	}

	private void resetParser()
	{
		ddm = null;
	}

	private boolean isLineToSkip(String line)
	{
		for (String toSkip : linesToSkip)
		{
			if (line.startsWith(toSkip))
			{
				return true;
			}
		}

		return line.startsWith(PREDIT_COMMENT_START);
	}
}
