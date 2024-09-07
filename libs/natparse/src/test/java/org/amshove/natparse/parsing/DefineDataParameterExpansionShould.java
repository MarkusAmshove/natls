package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IDefineData;
import org.amshove.natparse.natural.IVariableNode;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

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
	void addParameterFromUsings()
	{
		addUsing("MYPDA", """
			DEFINE DATA PARAMETER
			1 #PDA-PARM-1 (A10)
			1 #PDA-PARM-2 (N5)
			END-DEFINE
			""");

		assertParameterInOrder(
			"""
			PARAMETER USING MYPDA
			""",
			"#PDA-PARM-1",
			"#PDA-PARM-2"
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

	private void addUsing(String pdaName, String pdaSource)
	{
		var file = new NaturalFile(pdaName, Path.of(pdaName), NaturalFileType.PDA);
		var module = new NaturalModule(file);
		module.setDefineData(assertParsesWithoutDiagnostics(pdaSource));
		moduleProvider.addModule(pdaName, module);
	}
}
