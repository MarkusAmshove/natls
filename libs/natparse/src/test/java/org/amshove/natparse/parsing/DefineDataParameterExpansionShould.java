package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IDefineData;
import org.amshove.natparse.natural.IVariableNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class DefineDataParameterExpansionShould extends AbstractParserTest<IDefineData>
{
	DefineDataParameterExpansionShould()
	{
		super(DefineDataParser::new);
		useStubModuleProvider();
	}

	@Test
	void expandNonExpandableVariables()
	{
		assertParameterInOrder(
			"""
			PARAMETER
			1 #P-PARM-1 (A10)
			1 #P-PARM-2 (A20)
			""",
			"#P-PARM-1",
			"#P-PARM-2"
		);
	}

	@Test
	void expandGroups()
	{
		assertParameterInOrder(
			"""
			PARAMETER
			1 #P-GRP
			2 #P-PARM-1 (A10)
			2 #P-PARM-2 (A20)
			""",
			"#P-PARM-1",
			"#P-PARM-2"
		);
	}

	@Test
	void notExpandRedefineMembers()
	{
		assertParameterInOrder(
			"""
			PARAMETER
			1 #P-PARM-1 (A10)
			1 REDEFINE #P-PARM-1
			2 #P-DEF (A5)
			""",
			"#P-PARM-1"
		);
	}

	private void assertParameterInOrder(String defineDataSource, String... variableNames)
	{
		var defineData = assertParsesWithoutDiagnostics("""
		DEFINE DATA
		%s
		END-DEFINE
		""".formatted(defineDataSource));
		var parameter = defineData.effectiveParameterInOrder();
		assertThat(parameter.stream().map(IVariableNode::name))
			.containsExactly(variableNames);
	}
}
