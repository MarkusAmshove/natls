package org.amshove.natlint.natparse.parsing.ddm;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.amshove.natlint.natparse.NaturalParseException;
import org.amshove.natlint.natparse.natural.ddm.*;
import org.amshove.natlint.natparse.parsing.ddm.text.LinewiseTextScanner;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DdmParser
{
	private static final String PREDIC_COMMENT_START = "*";

	private static final DdmMetadataParser metadataParser = new DdmMetadataParser();
	private static final FieldParser adabasFieldParser = new FieldParser();
	private static final SqlFieldParser sqlFieldParser = new SqlFieldParser();
	private static final SuperdescriptorChildParser superdescriptorChildParser = new SuperdescriptorChildParser();

	private FieldParser fieldParser;

	private static final List<String> linesToSkip = Lists.newArrayList(
		"DDM OUTPUT TERMINATED",
		"SOURCE FIELD(S)",
		"- - -- ---------------",
		"T L DB Name",
		"Natural Source Header",
		":CP");

	private DataDefinitionModule ddm;
	private List<SuperdescriptorChild> childrenToReference;

	public IDataDefinitionModule parseDdm(String content)
	{
		resetParser();
		String[] lines = content.split("[\\r\\n]+");
		LinewiseTextScanner scanner = new LinewiseTextScanner(lines);
		fieldParser = adabasFieldParser;

		ImmutableList.Builder<IDdmField> ddmFields = ImmutableList.builder();

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

			if (line.startsWith("TYPE:"))
			{
				ddm.setDdmType(parseDdmType(line));

				fieldParser = ddm.type() == DdmType.SQL
					? sqlFieldParser
					: adabasFieldParser;

				scanner.advance();
				continue;
			}

			DdmField field = parseField(scanner);
			if (field.fieldType() == FieldType.GROUP)
			{
				GroupField groupField = new GroupField(field);
				scanner.advance();
				ImmutableList.Builder<IDdmField> groupMembers = ImmutableList.builder();
				parseGroup(scanner, groupField, groupMembers);
				groupField.setChildren(groupMembers.build());
				ddmFields.add(groupField);
				continue;
			}

			if (field.descriptor() == DescriptorType.SUPERDESCRIPTOR)
			{
				field = parseSuperdescriptor(scanner, new Superdescriptor(field));
				ddmFields.add(field);
				continue;
			}

			ddmFields.add(field);
			scanner.advance();
		}

		ddm.setFields(ddmFields.build());

		for (SuperdescriptorChild child : childrenToReference)
		{
			if (!setMatchingReference(child, ddm.fields()))
			{
				throw new NaturalParseException(String.format(
					"Could not find field referenced by superdescriptor child \"%s\"",
					child.name())
				);
			}
		}

		return ddm;
	}

	private boolean setMatchingReference(SuperdescriptorChild child, List<IDdmField> fields)
	{
		for (IDdmField field : fields)
		{
			if (field instanceof IGroupField
				&& setMatchingReference(child, ((IGroupField) field).members()))
			{
				return true;
			}

			if (field.name().equals(child.name()))
			{
				child.setField(field);
				return true;
			}
		}

		return false;
	}

	private static DdmType parseDdmType(String line)
	{
		return DdmType.valueOf(line.replace("TYPE:", "").trim());
	}

	private DdmField parseField(LinewiseTextScanner scanner)
	{
		try
		{
			return fieldParser.parse(scanner);
		}
		catch (Exception e)
		{
			throw new NaturalParseException(e, scanner.currentLineNumber());
		}
	}

	private void parseGroup(
		LinewiseTextScanner scanner,
		GroupField currentField,
		ImmutableList.Builder<IDdmField> groupMembers)
	{
		while (!scanner.isAtEnd())
		{
			if (isLineToSkip(scanner.peek()))
			{
				return;
			}

			DdmField nextField = parseField(scanner);

			if (nextField.level() <= currentField.level())
			{
				break;
			}

			if (nextField.fieldType() != FieldType.GROUP)
			{
				groupMembers.add(nextField);
				scanner.advance();
			}
			else
			{
				GroupField childGroupField = new GroupField(nextField);
				groupMembers.add(childGroupField);
				scanner.advance();
				ImmutableList.Builder<IDdmField> childGroupMembers = ImmutableList.builder();
				parseGroup(scanner, childGroupField, childGroupMembers);
				childGroupField.setChildren(childGroupMembers.build());
			}
		}
	}

	private Superdescriptor parseSuperdescriptor(LinewiseTextScanner scanner, DdmField field)
	{
		scanner.advance();
		// SOURCE FIELD(S) comment from predic
		scanner.advance();

		Superdescriptor superdescriptor = new Superdescriptor(field);
		ImmutableList.Builder<ISuperdescriptorChild> children = ImmutableList.builder();

		while (!scanner.isAtEnd() && containsSuperdescriptorSourceFieldRange(scanner.peek()))
		{
			SuperdescriptorChild child = superdescriptorChildParser.parse(scanner.peek());
			children.add(child);
			childrenToReference.add(child);
			scanner.advance();
		}

		superdescriptor.setChildren(children.build());

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
		childrenToReference = new ArrayList<>();
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

		return line.startsWith(PREDIC_COMMENT_START) || line.trim().isEmpty();
	}
}
