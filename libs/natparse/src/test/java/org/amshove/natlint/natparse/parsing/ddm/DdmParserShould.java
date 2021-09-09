package org.amshove.natlint.natparse.parsing.ddm;

import com.google.common.collect.ImmutableList;
import org.amshove.natlint.natparse.NaturalParseException;
import org.amshove.natlint.natparse.ResourceHelper;
import org.amshove.natlint.natparse.natural.ddm.*;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

public class DdmParserShould
{
	@Test
	void parseTheMetadataLine()
	{
		DataDefinitionModule dataDefinitionModule = new DdmParser().parseDdm("DB: 000 FILE: 128 - MY-EXCITING-DDM DEFAULT SEQUENCE: BH");

		assertThat(dataDefinitionModule.name()).isEqualTo("MY-EXCITING-DDM");
		assertThat(dataDefinitionModule.fileNumber()).isEqualTo("128");
		assertThat(dataDefinitionModule.databaseNumber()).isEqualTo("000");
		assertThat(dataDefinitionModule.defaultSequence()).isEqualTo("BH");
	}

	@Test
	void addLineInformationToParseExceptions()
	{
		assertThatExceptionOfType(NaturalParseException.class)
			.isThrownBy(() -> new DdmParser().parseDdm(ResourceHelper.readRelativeResourceFile("InvalidLevel.NSD", DdmParserShould.class)))
			.withMessage("Error at line 14: java.lang.NumberFormatException: For input string: \"A\"");
	}

	@Test
	void parseASqlDdmWithTrailingSpaceInAnEmptyLine()
	{
		assertThat(new DdmParser().parseDdm(ResourceHelper.readRelativeResourceFile("SqlEmptyLineWithTrailingSpace.NSD", DdmParserShould.class)))
			.isNotNull();
	}

	@Test
	void parseACompleteDdm()
	{
		String source = ResourceHelper.readRelativeResourceFile("CompleteDdm.NSD", DdmParserShould.class);
		IDataDefinitionModule ddm = new DdmParser().parseDdm(source);

		assertThat(ddm.name()).isEqualTo("COMPLETE-DDM");
		assertThat(ddm.fileNumber()).isEqualTo("100");
		assertThat(ddm.databaseNumber()).isEqualTo("000");
		assertThat(ddm.defaultSequence()).isEqualTo("");
		assertThat(ddm.type()).isEqualTo(DdmType.ADABAS);

		ImmutableList<IDdmField> fields = ddm.fields();

		assertThat(ddm.fields().size()).isEqualTo(10);
		List<String> topLevelFields = fields.stream().map(IDdmField::name).collect(Collectors.toList());

		assertThat(topLevelFields)
			.containsAll(Lists.newArrayList(
				"SOME-NUMBER",
				"ANOTHER-NUMBER",
				"ALPHA-FIELD",
				"ALPHANUMERIC-DESCRIPTOR",
				"NUMERIC-DESCRIPTOR",
				"NUMERIC-WITH-FLOATING",
				"A-SUPERDESCRIPTOR",
				"ANOTHER-SUPERDESCRIPTOR",
				"SUPERDESCRIPTOR-WITH-SUBRANGE"));

		IDdmField topLevelGroup = findField(ddm, "TOP-LEVEL-GROUP");
		IGroupField topLevelGroupField = assertIsGroupField(topLevelGroup);
		assertThat(topLevelGroupField.level()).isEqualTo(1);
		assertGroupHasMember(topLevelGroupField, "TOP-LEVEL-GROUP-CHILD", "TOP-LEVEL-GROUP-GROUP");

		IGroupField nestedGroup = assertIsGroupField(findGroupMember(topLevelGroupField, "TOP-LEVEL-GROUP-GROUP"));
		assertThat(nestedGroup.level()).isEqualTo(2);
		assertGroupHasMember(nestedGroup, "TOP-LEVEL-GROUP-GROUP-CHILD");

		ISuperdescriptor aSuperdescriptor = assertIsSuperdescriptor(findField(ddm, "A-SUPERDESCRIPTOR"));
		assertThat(aSuperdescriptor.fields()).hasSize(2);
		assertSuperdescriptorHasField(aSuperdescriptor, "ALPHA-FIELD",1,8);
		assertSuperdescriptorHasField(aSuperdescriptor, "ANOTHER-NUMBER",1,12);

		ISuperdescriptor anotherSuperdescriptor = assertIsSuperdescriptor(findField(ddm, "ANOTHER-SUPERDESCRIPTOR"));
		assertThat(anotherSuperdescriptor.fields()).hasSize(2);
		assertSuperdescriptorHasField(anotherSuperdescriptor, "ALPHA-FIELD",1,8);
		assertSuperdescriptorHasField(anotherSuperdescriptor, "ANOTHER-NUMBER",1,12);

		ISuperdescriptor superdescriptorWithSubrange = assertIsSuperdescriptor(findField(ddm, "SUPERDESCRIPTOR-WITH-SUBRANGE"));
		assertThat(superdescriptorWithSubrange.fields()).hasSize(2);
		assertSuperdescriptorHasField(superdescriptorWithSubrange, "SOME-NUMBER",1,5);
		assertSuperdescriptorHasField(superdescriptorWithSubrange, "ANOTHER-NUMBER-NUMBER",5,12);
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

	private void assertGroupHasMember(IGroupField groupField, String... memberNames)
	{
		assertThat(groupField.members())
			.as(String.format("Expected group to have exactly %d members, but had %d", memberNames.length, groupField.members().size()))
			.hasSize(memberNames.length);

		List<String> nextLevelGroupMembers = groupField.members().stream().map(IDdmField::name).collect(Collectors.toList());
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
		Optional<ISuperdescriptorChild> foundChild = superdescriptor.fields().stream().filter(f -> f.name().equals(fieldname)).findFirst();
		assertThat(foundChild).isPresent();
		ISuperdescriptorChild child = foundChild.get();

		assertThat(child.rangeFrom()).isEqualTo(expectedRangeFrom);
		assertThat(child.rangeTo()).isEqualTo(expectedRangeTo);
	}
}
