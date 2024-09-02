package org.amshove.natparse.parsing.ddm;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.amshove.natparse.NaturalParseException;
import org.amshove.natparse.natural.ddm.*;
import org.amshove.natparse.parsing.text.LinewiseTextScanner;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DdmParser
{
	private static final String PREDIC_COMMENT_START = "*";

	private static final DdmMetadataParser metadataParser = new DdmMetadataParser();
	private static final FieldParser adabasFieldParser = new FieldParser();
	private static final SqlFieldParser sqlFieldParser = new SqlFieldParser();
	private static final SDescriptorChildParser superdescriptorChildParser = new SDescriptorChildParser();

	private FieldParser fieldParser;

	private static final List<String> linesToSkip = Lists.newArrayList(
		"DDM OUTPUT TERMINATED",
		"                              Cataloged by",
		"       EM=",
		"       HD=",
		"SOURCE FIELD(S)",
		"- - -- ---------------",
		"T L DB Name",
		"Natural Source Header",
		"CODEPAGE:",
		":CP"
	);

	private DataDefinitionModule ddm;
	private List<SDescriptor> sDescriptorsToResolve;

	public IDataDefinitionModule parseDdm(String content)
	{
		resetParser();
		var lines = content.split("[\\r\\n]+");
		var scanner = new LinewiseTextScanner(lines);
		fieldParser = adabasFieldParser;

		ImmutableList.Builder<IDdmField> ddmFields = ImmutableList.builder();

		while (!scanner.isAtEnd())
		{
			var line = scanner.peek();

			if (isLineToSkip(line))
			{
				scanner.advance();
				continue;
			}

			if (line.startsWith("DB:"))
			{
				var metadata = metadataParser.parseMetadataLine(line);
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

			var field = parseField(scanner);
			if ((field.fieldType() == FieldType.GROUP || field.fieldType() == FieldType.PERIODIC)
				&& field.descriptor() != DescriptorType._S_DESCRIPTOR)
			{
				var groupField = new GroupField(field);
				scanner.advance();
				ImmutableList.Builder<IDdmField> groupMembers = ImmutableList.builder();
				parseGroup(scanner, groupField, groupMembers);
				groupField.setChildren(groupMembers.build());
				ddmFields.add(groupField);
				continue;
			}

			if (field.descriptor() == DescriptorType._S_DESCRIPTOR)
			{
				field = parseSuperdescriptor(scanner, new SDescriptor(field));
				ddmFields.add(field);
				continue;
			}

			ddmFields.add(field);
			scanner.advance();
		}

		ddm.setFields(ddmFields.build());

		for (var sDescriptor : sDescriptorsToResolve)
		{
			var matchCount = 0;
			var notFound = new ArrayList<String>();
			for (var child : sDescriptor.fields())
			{
				if (setMatchingReference(child, ddm.fields()))
				{
					matchCount += 1;
				}
				else
				{
					notFound.add(child.name());
				}
			}

			if (matchCount == 0)
			{
				sDescriptor.resolveDescriptorType(DescriptorType.SUBDESCRIPTOR);
				// TODO: Add the fields?
			}
			else
				if (matchCount == sDescriptor.fields().size())
				{
					sDescriptor.resolveDescriptorType(DescriptorType.SUPERDESCRIPTOR);
				}
				else
				{
					throw new NaturalParseException(
						String.format(
							"Could not find field(s) referenced by superdescriptor children [\"%s\"]",
							String.join("\",\"", notFound)
						)
					);
				}

		}

		return ddm;
	}

	private boolean setMatchingReference(ISDescriptorChild child, List<IDdmField> fields)
	{
		for (var field : fields)
		{
			if (field instanceof IGroupField
				&& setMatchingReference(child, ((IGroupField) field).members()))
			{
				return true;
			}

			if (field.name().equals(child.name()))
			{
				((SDescriptorChild) child).setField(field);
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
		ImmutableList.Builder<IDdmField> groupMembers
	)
	{
		while (!scanner.isAtEnd())
		{
			if (isLineToSkip(scanner.peek()))
			{
				return;
			}

			var nextField = parseField(scanner);

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
				var childGroupField = new GroupField(nextField);
				groupMembers.add(childGroupField);
				scanner.advance();
				ImmutableList.Builder<IDdmField> childGroupMembers = ImmutableList.builder();
				parseGroup(scanner, childGroupField, childGroupMembers);
				childGroupField.setChildren(childGroupMembers.build());
			}
		}
	}

	private SDescriptor parseSuperdescriptor(LinewiseTextScanner scanner, DdmField field)
	{
		scanner.advance();
		// SOURCE FIELD(S) comment from predic
		scanner.advance();

		var superdescriptor = new SDescriptor(field);
		ImmutableList.Builder<ISDescriptorChild> children = ImmutableList.builder();

		while (!scanner.isAtEnd() && containsSuperdescriptorSourceFieldRange(scanner.peek()))
		{
			var child = superdescriptorChildParser.parse(scanner.peek());
			children.add(child);
			scanner.advance();
		}

		superdescriptor.setChildren(children.build());
		sDescriptorsToResolve.add(superdescriptor);

		return superdescriptor;
	}

	private static final Pattern _S_DESCRIPTOR_CHILD_RANGE_PATTERN = Pattern.compile("^.*\\(\\d+-\\d+\\).*$");

	private static boolean containsSuperdescriptorSourceFieldRange(String line)
	{
		return _S_DESCRIPTOR_CHILD_RANGE_PATTERN.matcher(line).matches();
	}

	private void resetParser()
	{
		ddm = null;
		sDescriptorsToResolve = new ArrayList<>();
	}

	private boolean isLineToSkip(String line)
	{
		for (var toSkip : linesToSkip)
		{
			if (line.startsWith(toSkip))
			{
				return true;
			}
		}

		return line.startsWith(PREDIC_COMMENT_START) || line.trim().isEmpty();
	}
}
