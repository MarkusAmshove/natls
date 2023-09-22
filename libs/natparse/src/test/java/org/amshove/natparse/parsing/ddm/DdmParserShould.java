package org.amshove.natparse.parsing.ddm;

import org.amshove.natparse.NaturalParseException;
import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.ddm.*;
import org.amshove.testhelpers.ResourceHelper;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

class DdmParserShould
{
	@Test
	void parseTheMetadataLine()
	{
		var dataDefinitionModule = new DdmParser().parseDdm("DB: 000 FILE: 128 - MY-EXCITING-DDM DEFAULT SEQUENCE: BH");

		assertThat(dataDefinitionModule.name()).isEqualTo("MY-EXCITING-DDM");
		assertThat(dataDefinitionModule.fileNumber()).isEqualTo("128");
		assertThat(dataDefinitionModule.databaseNumber()).isEqualTo("000");
		assertThat(dataDefinitionModule.defaultSequence()).isEqualTo("BH");
	}

	@Test
	void addLineInformationToParseExceptions()
	{
		assertThatExceptionOfType(NaturalParseException.class)
			.isThrownBy(() -> parseFromResource("InvalidLevel.NSD"))
			.withMessage("Error at line 14: java.lang.NumberFormatException: For input string: \"A\"");
	}

	@Test
	void parseASqlDdmWithTrailingSpaceInAnEmptyLine()
	{
		assertThat(parseFromResource("SqlEmptyLineWithTrailingSpace.NSD"))
			.isNotNull();
	}

	@Test
	void parseACompleteDdm()
	{
		var ddm = parseFromResource("CompleteDdm.NSD");

		assertThat(ddm.name()).isEqualTo("COMPLETE-DDM");
		assertThat(ddm.fileNumber()).isEqualTo("100");
		assertThat(ddm.databaseNumber()).isEqualTo("000");
		assertThat(ddm.defaultSequence()).isEmpty();
		assertThat(ddm.type()).isEqualTo(DdmType.ADABAS);

		var fields = ddm.fields();

		assertThat(ddm.fields()).hasSize(12);
		var topLevelFields = fields.stream().map(IDdmField::name).collect(Collectors.toList());

		assertThat(topLevelFields)
			.containsAll(
				Lists.newArrayList(
					"SOME-NUMBER",
					"ANOTHER-NUMBER",
					"ALPHA-FIELD",
					"ALPHANUMERIC-DESCRIPTOR",
					"NUMERIC-DESCRIPTOR",
					"NUMERIC-WITH-FLOATING",
					"A-SUPERDESCRIPTOR",
					"ANOTHER-SUPERDESCRIPTOR",
					"SUPERDESCRIPTOR-WITH-SUBRANGE",
					"UNIQUE-SUPER-WITH-SUBRANGE"
				)
			);

		var topLevelGroup = findField(ddm, "TOP-LEVEL-GROUP");
		var topLevelGroupField = assertIsGroupField(topLevelGroup);
		assertThat(topLevelGroupField.level()).isEqualTo(1);
		assertGroupHasMember(topLevelGroupField, "TOP-LEVEL-GROUP-CHILD", "TOP-LEVEL-GROUP-GROUP");

		var nestedGroup = assertIsGroupField(findGroupMember(topLevelGroupField, "TOP-LEVEL-GROUP-GROUP"));
		assertThat(nestedGroup.level()).isEqualTo(2);
		assertGroupHasMember(nestedGroup, "TOP-LEVEL-GROUP-GROUP-CHILD");

		var periodicGroup = assertIsPeriodicGroupField(findField(ddm, "PERIODIC-GROUP"));
		assertThat(periodicGroup.fieldType()).isEqualTo(FieldType.PERIODIC);
		assertGroupHasMember(periodicGroup, "PERIODIC-MEMBER");
		var periodicMember = findGroupMember(periodicGroup, "PERIODIC-MEMBER");
		assertThat(periodicMember.level()).isEqualTo(2);
		assertThat(periodicMember.format()).isEqualTo(DataFormat.NUMERIC);
		assertThat(periodicMember.length()).isEqualTo(2.0);

		var aSuperdescriptor = assertIsSuperdescriptor(findField(ddm, "A-SUPERDESCRIPTOR"));
		assertThat(aSuperdescriptor.fields()).hasSize(2);
		assertSuperdescriptorHasField(aSuperdescriptor, "ALPHA-FIELD", 1, 8);
		assertSuperdescriptorHasField(aSuperdescriptor, "ANOTHER-NUMBER", 1, 12);

		var anotherSuperdescriptor = assertIsSuperdescriptor(findField(ddm, "ANOTHER-SUPERDESCRIPTOR"));
		assertThat(anotherSuperdescriptor.fields()).hasSize(2);
		assertSuperdescriptorHasField(anotherSuperdescriptor, "ALPHA-FIELD", 1, 8);
		assertSuperdescriptorHasField(anotherSuperdescriptor, "ANOTHER-NUMBER", 1, 12);

		var superdescriptorWithSubrange = assertIsSuperdescriptor(findField(ddm, "SUPERDESCRIPTOR-WITH-SUBRANGE"));
		assertThat(superdescriptorWithSubrange.fields()).hasSize(2);
		assertSuperdescriptorHasField(superdescriptorWithSubrange, "SOME-NUMBER", 1, 5);
		assertSuperdescriptorHasField(superdescriptorWithSubrange, "TOP-LEVEL-GROUP-CHILD", 5, 12);
	}

	@Test
	void parseAComplexSqlDdm()
	{
		var ddm = parseFromResource("ComplexSqlTypeDdm.NSD");
		assertThat(ddm.type()).isEqualTo(DdmType.SQL);
		assertThat(findField(ddm, "ID").descriptor()).isEqualTo(DescriptorType.DESCRIPTOR);
		assertThat(findField(ddm, "L@LONG-VARCHAR").shortname()).isEqualTo("I_");
		assertThat(findField(ddm, "LONG-VARCHAR").length()).isEqualTo(2500);
		assertThat(findField(ddm, "L@DYNAMIC-CLOB").format()).isEqualTo(DataFormat.INTEGER);
		assertThat(findField(ddm, "DYNAMIC-CLOB").length()).isEqualTo(9999);
		assertThat(findField(ddm, "N@DYNAMIC-CLOB").length()).isEqualTo(2);
	}

	@Test
	void parseAComplexSqlDdmAndUseIt()
	{
		var ddm = parseFromResource("DdmFromTrygVFF_OPLYSNING.NSD");
		assertThat(findField(ddm, "FF_NR").descriptor()).isEqualTo(DescriptorType.DESCRIPTOR);
		assertThat(findField(ddm, "FF_NR").format()).isEqualTo(DataFormat.PACKED);
		//assertThat(ddm.type()).isEqualTo(DdmType.SQL); TODO(lexermode): DdmType is undefined because Type: is not present in the DDM source. What can we do?
	}

	@Test
	void referneceFieldsFromSuperdescriptorChilds()
	{
		var ddm = parseFromResource("SuperdescriptorChildReference.NSD");

		var descriptor = assertIsSuperdescriptor(findField(ddm, "A-SUPERDESCRIPTOR"));

		var firstField = findField(ddm, "ALPHA-FIELD");
		var secondField = findField(ddm, "ANOTHER-NUMBER");

		var firstChild = findSuperdescriptorChild(descriptor, "ALPHA-FIELD");
		var secondChild = findSuperdescriptorChild(descriptor, "ANOTHER-NUMBER");

		assertThat(firstChild.field()).isEqualTo(firstField);
		assertThat(secondChild.field()).isEqualTo(secondField);
	}

	@Test
	void parseASuperdescriptorThatIsAlsoAPeriodicGroup()
	{
		var ddm = new DdmParser().parseDdm("""
DB: 000 FILE: 100  - MY-DDM                      DEFAULT SEQUENCE:
TYPE: ADABAS

T L DB Name                              F Leng  S D Remark
- - -- --------------------------------  - ----  - - ------------------------
  1 AA A-DDM-FIELD                       A   10  N
  1 AB ANOTHER-DDM-FIELD                 A   15  N
M 1 AC A-MULTIPLE-FIELD                  N  7,2  N
P 1 BA A-PERIODIC-GROUP
  2 BB A-PERIODIC-MEMBER                 A    5  N
P 1 AG A-SUPERDESCRIPTOR                 A   25  N S
*      -------- SOURCE FIELD(S) -------
*      A-DDM-FIELD   (1-10)
*      ANOTHER-DDM-FIELD (1-15)
			""");

		var descriptor = findField(ddm, "A-SUPERDESCRIPTOR");
		assertThat(descriptor.format()).isEqualTo(DataFormat.ALPHANUMERIC);
		assertThat(descriptor.length()).isEqualTo(25);
		assertThat(descriptor.descriptor()).isEqualTo(DescriptorType.SUPERDESCRIPTOR);
		assertThat(descriptor.fieldType()).isEqualTo(FieldType.PERIODIC);
	}

	@Test
	void throwAnExceptionIfNoMatchingFieldToReferenceIsFound()
	{
		assertThatExceptionOfType(NaturalParseException.class)
			.isThrownBy(() -> parseFromResource("SuperdescriptorChildMissingReference.NSD"))
			.withMessage("Could not find field referenced by superdescriptor child \"ANOTHER-NUMBER\"");
	}

	private IDataDefinitionModule parseFromResource(String resourceName)
	{
		var resourceSource = ResourceHelper.readRelativeResourceFile(resourceName, DdmParserShould.class);
		return new DdmParser().parseDdm(resourceSource);
	}

	private IDdmField findField(IDataDefinitionModule ddm, String fieldname)
	{
		return ddm
			.fields()
			.stream()
			.filter(f -> f.name().equals(fieldname))
			.findFirst()
			.orElseThrow(() -> new RuntimeException("Couldn't find field " + fieldname));
	}

	private IDdmField findGroupMember(IGroupField groupField, String fieldname)
	{
		return groupField
			.members()
			.stream()
			.filter(f -> f.name().equals(fieldname))
			.findFirst()
			.orElseThrow(() -> new RuntimeException("Couldn't find group member " + fieldname));
	}

	private IGroupField assertIsGroupField(IDdmField field)
	{
		assertThat(field.fieldType()).isEqualTo(FieldType.GROUP);
		assertThat(field).isInstanceOf(IGroupField.class);
		return (IGroupField) field;
	}

	private IGroupField assertIsPeriodicGroupField(IDdmField field)
	{
		assertThat(field.fieldType()).isEqualTo(FieldType.PERIODIC);
		assertThat(field).isInstanceOf(IGroupField.class);
		return (IGroupField) field;
	}

	private void assertGroupHasMember(IGroupField groupField, String... memberNames)
	{
		assertThat(groupField.members())
			.as(String.format("Expected group to have exactly %d members, but had %d", memberNames.length, groupField.members().size()))
			.hasSize(memberNames.length);

		var nextLevelGroupMembers = groupField.members().stream().map(IDdmField::name).collect(Collectors.toList());
		assertThat(nextLevelGroupMembers)
			.containsAll(Lists.newArrayList(memberNames));
	}

	private ISuperdescriptor assertIsSuperdescriptor(IDdmField field)
	{
		assertThat(field.descriptor()).isEqualTo(DescriptorType.SUPERDESCRIPTOR);
		assertThat(field).isInstanceOf(ISuperdescriptor.class);
		return (ISuperdescriptor) field;
	}

	private void assertSuperdescriptorHasField(ISuperdescriptor superdescriptor, String fieldname, int expectedRangeFrom, int expectedRangeTo)
	{
		var child = findSuperdescriptorChild(superdescriptor, fieldname);

		assertThat(child.rangeFrom()).isEqualTo(expectedRangeFrom);
		assertThat(child.rangeTo()).isEqualTo(expectedRangeTo);
	}

	private ISuperdescriptorChild findSuperdescriptorChild(ISuperdescriptor superdescriptor, String fieldname)
	{
		var foundChild = superdescriptor.fields().stream().filter(f -> f.name().equals(fieldname)).findFirst();
		assertThat(foundChild).isPresent();
		return foundChild.get();
	}
}
