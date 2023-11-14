package org.amshove.natparse.parsing;

import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class VariableReferenceTests extends ParserIntegrationTest
{
	@Test
	void referencableVariablesShouldBeExported(@ProjectName("variablereferencetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModule("SUBMOD"), ISubprogram.class);
		assertThat(subprogram.referencableNodes()).anyMatch(r -> r instanceof IVariableNode variableNode && variableNode.name().equals("#LOCALVAR"));
	}

	@Test
	void localVariablesShouldBeReferenced(@ProjectName("variablereferencetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModule("SUBMOD"), ISubprogram.class);
		var variable = subprogram.defineData().findVariable("#LOCALVAR");
		assertThat(variable).isNotNull();

		assertThat(variable.references()).hasSize(3);
	}

	@Test
	void importedVariablesShouldBeReferenced(@ProjectName("variablereferencetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModule("SUBMOD2"), ISubprogram.class);
		var variable = subprogram.defineData().findVariable("#IMPORTED-VAR");
		assertThat(variable).isNotNull();

		assertThat(variable.references()).hasSize(3);
	}

	@Test
	void usingNodeShouldHaveAReferenceToIncludedModule(@ProjectName("variablereferencetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModule("SUBMOD2"), ISubprogram.class);
		var using = subprogram.defineData().usings().first();

		assertThat(using.reference()).isNotNull();
		assertThat(using.referencingToken().symbolName()).isEqualTo("MYLDA");
	}

	@Test
	void includeNodesShouldAddTheirTreeToTheInclude(@ProjectName("variablereferencetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModule("SUBMOD3"), ISubprogram.class);
		var variable = subprogram.defineData().findVariable("#INSIDE-INCLUDE");

		assertThat(variable).isNotNull();
		assertThat(variable.references()).hasSize(1);
	}

	@Test
	void includeShouldAddABidirectionalReference(@ProjectName("variablereferencetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModule("SUBMOD3"), ISubprogram.class);
		var include = subprogram.body().findDescendantOfType(IIncludeNode.class);

		assertThat(include).isNotNull();
		assertThat(include.reference().callers()).contains(include);
	}

	@Test
	void ignorePlusInVariableNameIfItMightNotBeAiv(@ProjectName("variablereferencetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModule("STRANGEAIV"), ISubprogram.class);

		assertThat(subprogram.diagnostics()).isEmpty();

		var varOne = subprogram.defineData().findVariable("#VARONE");
		assertThat(varOne).isNotNull();
		assertThat(varOne.references()).isNotEmpty();

		var varTwo = subprogram.defineData().findVariable("#VARTWO");
		assertThat(varTwo).isNotNull();
		assertThat(varTwo.references()).isNotEmpty();

		var varThree = subprogram.defineData().findVariable("#VARTHREE");
		assertThat(varThree).isNotNull();
		assertThat(varThree.references()).isNotEmpty();
	}

	@Test
	void addAReferenceToVariablesUsedAsCounter(@ProjectName("variablereferencetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModule("CSTAR"), ISubprogram.class);

		assertThat(subprogram.defineData().findVariable("#MYVAR").references()).isNotEmpty();
	}

	@Test
	void addAReferenceToVariablesUsedAsPStar(@ProjectName("variablereferencetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModule("PSTAR"), ISubprogram.class);

		assertThat(subprogram.defineData().findVariable("#MYVAR").references()).isNotEmpty();
	}

	@Test
	void addAReferenceToVariablesUsedAsTStar(@ProjectName("variablereferencetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModule("TSTAR"), ISubprogram.class);

		assertThat(subprogram.defineData().findVariable("#MYVAR").references()).isNotEmpty();
	}

	@Test
	void ambiguousVariableReferencesShouldBeAnnotated(@ProjectName("variablereferencetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModule("AMBIG"), ISubprogram.class);
		assertThat(subprogram.diagnostics())
			.as("Expected only one diagnostic")
			.hasSize(1);
		assertThat(subprogram.diagnostics())
			.as("The expected diagnostic id differs")
			.allMatch(d -> d.id().equals(ParserError.AMBIGUOUS_VARIABLE_REFERENCE.id()));
	}

	@Test
	void disambiguateAndResolveVariableReferencesBasedOnAdabasAccessContext(@ProjectName("variablereferencetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModule("DISAMBIG"), ISubprogram.class);
		var assignments = NodeUtil.findNodesOfType(subprogram.body(), IAssignmentStatementNode.class);
		assertThat(assignments).hasSize(2);

		var aViewDdmField = subprogram.defineData().findVariable("A-VIEW.DDMFIELD");
		var bViewDdmField = subprogram.defineData().findVariable("B-VIEW.DDMFIELD");

		assertThat(((IVariableReferenceNode) assignments.get(0).operand()).reference()).isEqualTo(aViewDdmField);
		assertThat(((IVariableReferenceNode) assignments.get(1).operand()).reference()).isEqualTo(bViewDdmField);
	}

	@Test
	void addReferenceToVariableOperand(@ProjectName("variablereferencetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModule("OPER"), ISubprogram.class);
		var forStatement = (IForLoopNode) subprogram.body().statements().first();
		assertThat(forStatement.upperBound()).isInstanceOf(IVariableReferenceNode.class);
		assertThat(((IVariableReferenceNode) forStatement.upperBound()).reference()).isNotNull();
	}

	@Test
	void addReferenceToVariableSystemFunctionParameterOperand(@ProjectName("variablereferencetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModule("OPER"), ISubprogram.class);
		var forStatement = (IForLoopNode) subprogram.body().statements().get(1);
		assertThat(forStatement.upperBound()).isInstanceOf(ISystemFunctionNode.class);
		var parameter = ((ISystemFunctionNode) forStatement.upperBound()).parameter().first();
		assertThat(parameter).isInstanceOf(IVariableReferenceNode.class);
		assertThat(((IVariableReferenceNode) parameter).reference()).isNotNull();
	}
}
