package org.amshove.natls.codelens;

import org.amshove.natls.CustomCommands;
import org.eclipse.lsp4j.CodeLens;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DisplayName("CodeLens for NatUnit should")
class NatUnitCodeLensTests extends CodeLensTest
{
	@Test
	void provideNoCodeLensIfNoTestcaseReferencesAModule()
	{
		var identifier = createOrSaveFile("LIBONE", "SUB.NSN", """
		DEFINE DATA LOCAL
		END-DEFINE
		END
		""");

		testCodeLens(identifier, lenses -> assertThat(filterNatUnitLenses(lenses)).isEmpty());
	}

	@Test
	void provideACodeLensCountingOneTestCaseWithMultipleReferences()
	{
		var identifier = createOrSaveFile("LIBONE", "SUB.NSN", """
		DEFINE DATA LOCAL
		END-DEFINE
		END
		""");

		createOrSaveFile("LIBONE", "TCTEST.NSN", """
		DEFINE DATA LOCAL
		END-DEFINE
		CALLNAT 'SUB'
		CALLNAT 'SUB'
		END
		""");

		testCodeLens(identifier, lenses ->
		{
			var natUnitLens = filterNatUnitLenses(lenses).get(0);
			assertThat(natUnitLens.getCommand().getTitle()).isEqualTo("$(beaker) 1 testcase");
		});
	}

	@Test
	void provideACodeLensCountingMultipleTestCaseWithMultipleReferences()
	{
		var identifier = createOrSaveFile("LIBONE", "SUB.NSN", """
		DEFINE DATA LOCAL
		END-DEFINE
		END
		""");

		createOrSaveFile("LIBONE", "TCTEST.NSN", """
		DEFINE DATA LOCAL
		END-DEFINE
		CALLNAT 'SUB'
		CALLNAT 'SUB'
		END
		""");

		createOrSaveFile("LIBONE", "TCTEST2.NSN", """
		DEFINE DATA LOCAL
		END-DEFINE
		CALLNAT 'SUB'
		CALLNAT 'SUB'
		END
		""");

		testCodeLens(identifier, lenses ->
		{
			var natUnitLens = filterNatUnitLenses(lenses).get(0);
			assertThat(natUnitLens.getCommand().getTitle()).isEqualTo("$(beaker) 2 testcases");
		});
	}

	private static List<? extends CodeLens> filterNatUnitLenses(List<? extends CodeLens> lenses)
	{
		return lenses.stream()
			.filter(l -> l.getCommand().getCommand().equals(CustomCommands.CODELENS_SHOW_TESTS))
			.toList();
	}
}
