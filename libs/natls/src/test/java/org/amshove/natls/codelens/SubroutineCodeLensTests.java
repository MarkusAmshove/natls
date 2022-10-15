package org.amshove.natls.codelens;

import org.amshove.natls.CustomCommands;
import org.amshove.natls.languageserver.LspUtil;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DisplayName("CodeLens for internal subroutiens should")
class SubroutineCodeLensTests extends CodeLensTest
{
	@Test
	void provideCodeLensWithNoReferencesIfASubroutineIsUnused()
	{
		var document = createOrSaveFile("LIBONE", "SUB.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			   
			DEFINE SUBROUTINE MY-SUB
			IGNORE
			END-SUBROUTINE
			END
			""");

		testCodeLens(document, lenses -> {
			assertThat(lenses).hasSize(2); // TODO(configuration): Once we can disable the others, this should be 1

			var lens = lenses.stream().filter(l -> l.getRange().getStart().getLine() > 0).findFirst().orElseThrow();
			var soft = new SoftAssertions();
			soft.assertThat(lens.getRange()).isEqualTo(LspUtil.newLineRange(3, 18, 24));
			soft.assertThat(lens.getCommand().getTitle()).as("Title").isEqualTo("No references");
			soft.assertThat(lens.getCommand().getArguments()).as("Arguments").isNull();
			soft.assertThat(lens.getCommand().getCommand()).as("Command").isEqualTo(CustomCommands.CODELENS_NON_INTERACTIVE);
			soft.assertAll();
		});
	}

	@Test
	void provideCodeLensWithMultipleReferences()
	{
		var document = createOrSaveFile("LIBONE", "SUB2.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			
			PERFORM MY-SUB
			   
			DEFINE SUBROUTINE MY-SUB
			IGNORE
			END-SUBROUTINE
			
			PERFORM MY-SUB
			PERFORM MY-SUB
			
			END
			""");

		testCodeLens(document, lenses -> {
			assertThat(lenses).hasSize(2); // TODO(configuration): Once we can disable the others, this should be 1

			var lens = lenses.stream().filter(l -> l.getRange().getStart().getLine() > 0).findFirst().orElseThrow();
			var soft = new SoftAssertions();
			var expectedRange = LspUtil.newLineRange(5, 18, 24);
			soft.assertThat(lens.getRange()).isEqualTo(expectedRange);
			soft.assertThat(lens.getCommand().getTitle()).as("Title").isEqualTo("3 references");
			soft.assertThat(lens.getCommand().getArguments()).as("Arguments").isEqualTo(Arrays.asList(document.getUri(), expectedRange));
			soft.assertThat(lens.getCommand().getCommand()).as("Command").isEqualTo(CustomCommands.CODELENS_SHOW_REFERENCES);
			soft.assertAll();
		});
	}
}
