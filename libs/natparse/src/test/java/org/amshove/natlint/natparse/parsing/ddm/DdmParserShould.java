package org.amshove.natlint.natparse.parsing.ddm;

import com.google.common.collect.ImmutableList;
import org.amshove.natlint.natparse.ResourceHelper;
import org.amshove.natlint.natparse.natural.ddm.FieldType;
import org.amshove.natlint.natparse.natural.ddm.IDataDefinitionModule;
import org.amshove.natlint.natparse.natural.ddm.IDdmField;
import org.amshove.natlint.natparse.natural.ddm.IGroupField;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

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
	void parseACompleteDdm()
	{
		String source = ResourceHelper.readRelativeResourceFile("CompleteDdm.NSD", DdmParserShould.class);
		IDataDefinitionModule ddm = new DdmParser().parseDdm(source);

		assertThat(ddm.name()).isEqualTo("COMPLETE-DDM");
		assertThat(ddm.fileNumber()).isEqualTo("100");
		assertThat(ddm.databaseNumber()).isEqualTo("000");
		assertThat(ddm.defaultSequence()).isEqualTo(" ");

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

		assertThat(true).as("Test descriptors").isFalse();
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
}
