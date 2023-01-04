package org.amshove.natls.codelens;

import org.amshove.natls.CustomCommands;
import org.amshove.natls.languageserver.LspUtil;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DisplayName("CodeLens for modules should")
class ModuleCodeLensTests extends CodeLensTest
{
	@Test
	void provideCodeLensWithNoReferencesIfAModuleIsUnused()
	{
		var identifier = createOrSaveFile("LIBONE", "SUB.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
						
			END
			""");

		testCodeLens(identifier, lenses ->
		{
			assertThat(lenses).hasSize(1);
			var lens = lenses.get(0);
			var soft = new SoftAssertions();
			soft.assertThat(lens.getRange()).as("Range").isEqualTo(LspUtil.newLineRange(0, 0, 6));
			soft.assertThat(lens.getCommand().getTitle()).isEqualTo("No references");
			soft.assertThat(lens.getCommand().getCommand()).as("Command").isEqualTo(CustomCommands.CODELENS_NON_INTERACTIVE);
			soft.assertThat(lens.getCommand().getArguments()).as("Arguments").isNull();
			soft.assertAll();
		});
	}

	@Test
	void provideCodeLensForAModuleWhichHasReferrers()
	{
		var identifier = createOrSaveFile("LIBONE", "SUB2.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			END
			""");

		createOrSaveFile("LIBONE", "SUBCALL.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			CALLNAT 'SUB2'
			END
			""");

		testCodeLens(identifier, lenses ->
		{
			assertThat(lenses).hasSize(1);
			var lens = lenses.get(0);
			var soft = new SoftAssertions();
			soft.assertThat(lens.getCommand().getTitle()).isEqualTo("1 references");
			var expectedRange = LspUtil.newLineRange(0, 0, 6);
			soft.assertThat(lens.getRange()).isEqualTo(expectedRange);
			soft.assertThat(lens.getCommand().getCommand()).as("Command").isEqualTo(CustomCommands.CODELENS_SHOW_REFERENCES);
			soft.assertThat(lens.getCommand().getArguments()).as("Arguments").isEqualTo(Arrays.asList(identifier.getUri(), expectedRange));
			soft.assertAll();
		});
	}
}
