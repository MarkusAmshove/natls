package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class VariableReferenceTests extends ParserIntegrationTest
{
	@Test
	void referencableVariablesShouldBeExported(@ProjectName("variablereferencetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModuleUnsafe("SUBMOD"), ISubprogram.class);
		assertThat(subprogram.referencableNodes()).anyMatch(r -> r instanceof IVariableNode variableNode && variableNode.name().equals("#LOCALVAR"));
	}

	@Test
	void localVariablesShouldBeReferenced(@ProjectName("variablereferencetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModuleUnsafe("SUBMOD"), ISubprogram.class);
		var variable = subprogram.defineData().findVariable("#LOCALVAR").orElseThrow();
		assertThat(variable).isNotNull();

		assertThat(variable.references()).hasSize(3);
	}

	@Test
	void importedVariablesShouldBeReferenced(@ProjectName("variablereferencetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModuleUnsafe("SUBMOD2"), ISubprogram.class);
		var variable = subprogram.defineData().findVariable("#IMPORTED-VAR").orElseThrow();
		assertThat(variable).isNotNull();

		assertThat(variable.references()).hasSize(3);
	}

	@Test
	void usingNodeShouldHaveAReferenceToIncludedModule(@ProjectName("variablereferencetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModuleUnsafe("SUBMOD2"), ISubprogram.class);
		var using = subprogram.defineData().usings().first();

		assertThat(using.reference()).isNotNull();
		assertThat(using.referencingToken().symbolName()).isEqualTo("MYLDA");
	}

	@Test
	void includeNodesShouldAddTheirTreeToTheInclude(@ProjectName("variablereferencetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModuleUnsafe("SUBMOD3"), ISubprogram.class);
		var variable = subprogram.defineData().findVariable("#INSIDE-INCLUDE").orElseThrow();

		assertThat(variable).isNotNull();
		assertThat(variable.references()).hasSize(1);
	}


	@Test
	void includeShouldAddABidirectionalReference(@ProjectName("variablereferencetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModuleUnsafe("SUBMOD3"), ISubprogram.class);
		var include = subprogram.body().findDescendantOfType(IIncludeNode.class).orElseThrow();

		assertThat(include).isNotNull();
		assertThat(include.reference().callers()).contains(include);
	}

	@Test
	void ignorePlusInVariableNameIfItMightNotBeAiv(@ProjectName("variablereferencetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModuleUnsafe("STRANGEAIV"), ISubprogram.class);

		assertThat(subprogram.diagnostics()).isEmpty();

		var varOne = subprogram.defineData().findVariable("#VARONE").orElseThrow();
		assertThat(varOne).isNotNull();
		assertThat(varOne.references()).isNotEmpty();

		var varTwo = subprogram.defineData().findVariable("#VARTWO").orElseThrow();
		assertThat(varTwo).isNotNull();
		assertThat(varTwo.references()).isNotEmpty();

		var varThree = subprogram.defineData().findVariable("#VARTHREE").orElseThrow();
		assertThat(varThree).isNotNull();
		assertThat(varThree.references()).isNotEmpty();
	}

	@Test
	void addAReferenceToVariablesUsedAsCounter(@ProjectName("variablereferencetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModuleUnsafe("CSTAR"), ISubprogram.class);

		assertThat(subprogram.defineData().findVariable("#MYVAR").orElseThrow().references()).isNotEmpty();
	}

	@Test
	void addAReferenceToVariablesUsedAsPStar(@ProjectName("variablereferencetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModuleUnsafe("PSTAR"), ISubprogram.class);

		assertThat(subprogram.defineData().findVariable("#MYVAR").orElseThrow().references()).isNotEmpty();
	}

	@Test
	void addAReferenceToVariablesUsedAsTStar(@ProjectName("variablereferencetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModuleUnsafe("TSTAR"), ISubprogram.class);

		assertThat(subprogram.defineData().findVariable("#MYVAR").orElseThrow().references()).isNotEmpty();
	}

	@Test
	void ambiguousVariableReferencesShouldBeAnnotated(@ProjectName("variablereferencetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModuleUnsafe("AMBIG"), ISubprogram.class);
		assertThat(subprogram.diagnostics())
			.as("Expected only one diagnostic")
			.hasSize(1);
		assertThat(subprogram.diagnostics())
			.as("The expected diagnostic id differs")
			.allMatch(d -> d.id().equals(ParserError.AMBIGUOUS_VARIABLE_REFERENCE.id()));
	}

	@Test
	void addReferenceToVariableOperand(@ProjectName("variablereferencetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModuleUnsafe("OPER"), ISubprogram.class);
		var forStatement = (IForLoopNode) subprogram.body().statements().first();
		assertThat(forStatement.upperBound()).isInstanceOf(IVariableReferenceNode.class);
		assertThat(((IVariableReferenceNode) forStatement.upperBound()).reference()).isNotNull();
	}

	@Test
	void addReferenceToVariableSystemFunctionParameterOperand(@ProjectName("variablereferencetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModuleUnsafe("OPER"), ISubprogram.class);
		var forStatement = (IForLoopNode) subprogram.body().statements().get(1);
		assertThat(forStatement.upperBound()).isInstanceOf(ISystemFunctionNode.class);
		var parameter = ((ISystemFunctionNode) forStatement.upperBound()).parameter().first();
		assertThat(parameter).isInstanceOf(IVariableReferenceNode.class);
		assertThat(((IVariableReferenceNode) parameter).reference()).isNotNull();
	}
}
