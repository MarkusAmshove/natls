package org.amshove.natls.project;

import org.amshove.natls.testlifecycle.LanguageServerTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTest;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.amshove.natparse.natural.IHasDefineData;
import org.amshove.natparse.natural.ISubprogram;
import org.amshove.natparse.natural.IViewNode;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@LspTest
class LanguageServerFileAsModuleProviderShould extends LanguageServerTest
{
	private static LspTestContext testContext;

	@BeforeAll
	static void setupProject(@LspProjectName("module_provider_tests") LspTestContext context)
	{
		testContext = context;
	}

	@Test
	void findAModuleWhenAskedForAModule()
	{
		var module = testContext.project().findFileByReferableName("SUB");
		assertThat(module.getType()).isEqualTo(NaturalFileType.SUBPROGRAM);
		assertThat(module.module()).isInstanceOf(ISubprogram.class);
	}

	@Test
	void findADdmWhenAskedForADdm()
	{
		var module = testContext.project().findFileByReferableName("SUB");
		var ddm = module.findDdm("SUB");
		assertThat(ddm).isNotNull();
		assertThat(ddm.fields()).hasSize(1);
	}

	@Test
	void haveTheDdmResolvedForTheView()
	{
		var module = testContext.project().findFileByReferableName("SUB");
		var parsedModule = (IHasDefineData) module.module();
		var theView = (IViewNode) parsedModule.defineData().findVariable("THEVIEW");
		assertThat(theView.ddm()).isNotNull();
	}

	@Override
	protected LspTestContext getContext()
	{
		return testContext;
	}
}
