package org.amshove.natparse.parsing;

import org.amshove.natparse.ProjectName;
import org.amshove.natparse.natural.IIncludeNode;
import org.amshove.natparse.natural.ISubprogram;
import org.amshove.natparse.natural.project.NaturalProject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class VariableReferenceTests extends ParserIntegrationTest
{
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
}
