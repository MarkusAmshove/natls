package org.amshove.natls.inlayhints;

import org.amshove.natls.config.LSConfiguration;
import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natls.testlifecycle.LanguageServerTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.eclipse.lsp4j.InlayHintKind;
import org.eclipse.lsp4j.InlayHintParams;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class InlayHintingTests extends LanguageServerTest
{

	@Test
	void inlayHintsWithSubroutineNameShouldBeShownAtEndSubroutine()
	{
		var td = createOrSaveFile("LIBONE", "MYMODULE.NSN", """
			DEFINE DATA
			END-DEFINE

			DEFINE SUBROUTINE MY-SUBROUTINE
				IGNORE
			END-SUBROUTINE
			END
			""");

		var request = getContext().documentService().inlayHint(new InlayHintParams(td, LspUtil.newRange(0, 0, 5, 0)));
		assertThat(request)
			.succeedsWithin(1, TimeUnit.SECONDS)
			.satisfies(
				hints -> assertThat(hints).hasSize(1),
				hints -> assertThat(hints.get(0).getKind()).isEqualTo(InlayHintKind.Type),
				hints -> assertThat(hints.get(0).getLabel().getLeft()).isEqualTo("MY-SUBROUTINE"),
				hints -> assertThat(hints.get(0).getPaddingLeft()).isTrue(),
				hints -> assertThat(hints.get(0).getPosition().getLine()).isEqualTo(5),
				hints -> assertThat(hints.get(0).getPosition().getCharacter()).isEqualTo(14)
			);
	}

	@Test
	void inlayHintsShouldBeAddedToPerformsThatCallALocalSubroutineThatIsNotDeclaredInTheSameFile()
	{
		createOrSaveFile("LIBONE", "CCODE.NSC", """
			DEFINE SUBROUTINE IN-COPYCODE
			IGNORE
			END-SUBROUTINE
			""");
		var td = createOrSaveFile("LIBONE", "MYMODULE.NSN", """
			DEFINE DATA
			END-DEFINE

			INCLUDE CCODE

			PERFORM IN-COPYCODE
			END
			""");

		var request = getContext().documentService().inlayHint(new InlayHintParams(td, LspUtil.newRange(0, 0, 5, 0)));
		assertThat(request)
			.succeedsWithin(1, TimeUnit.SECONDS)
			.satisfies(
				hints -> assertThat(hints).hasSize(1),
				hints -> assertThat(hints.get(0).getKind()).isEqualTo(InlayHintKind.Type),
				hints -> assertThat(hints.get(0).getLabel().getLeft()).isEqualTo("CCODE"),
				hints -> assertThat(hints.get(0).getPaddingLeft()).isTrue(),
				hints -> assertThat(hints.get(0).getPosition().getLine()).isEqualTo(5),
				hints -> assertThat(hints.get(0).getPosition().getCharacter()).isEqualTo(7)
			);
	}

	@Test
	void inlayHintsShouldBeAddedForTheTargetVariableOnAssignmentsWhenEnabled()
	{
		var config = LSConfiguration.createDefault();
		config.getInlayhints().setShowAssignmentTargetType(true);
		configureLSConfig(config);

		var td = createOrSaveFile("LIBONE", "MYMODULE.NSN", """
			DEFINE DATA LOCAL
			1 #VAR (A10)
			END-DEFINE
			#VAR := 'Hi'
			END
			""");

		var request = getContext().documentService().inlayHint(new InlayHintParams(td, LspUtil.newRange(0, 0, 5, 0)));
		assertThat(request)
			.succeedsWithin(1, TimeUnit.SECONDS)
			.satisfies(
				hints -> assertThat(hints).hasSize(1),
				hints -> assertThat(hints.get(0).getKind()).isEqualTo(InlayHintKind.Type),
				hints -> assertThat(hints.get(0).getLabel().getLeft()).isEqualTo("(A10)"),
				hints -> assertThat(hints.get(0).getPaddingLeft()).isTrue(),
				hints -> assertThat(hints.get(0).getPosition().getLine()).isEqualTo(3),
				hints -> assertThat(hints.get(0).getPosition().getCharacter()).isEqualTo(4)
			);
	}

	@Test
	void inlayHintsShouldNotBeAddedForTheTargetVariableOnAssignmentsWhenHintsAreDisabled()
	{
		var config = LSConfiguration.createDefault();
		config.getInlayhints().setShowAssignmentTargetType(false);
		configureLSConfig(config);

		var td = createOrSaveFile("LIBONE", "MYMODULE.NSN", """
			DEFINE DATA LOCAL
			1 #VAR (A10)
			END-DEFINE
			#VAR := 'Hi'
			END
			""");

		var request = getContext().documentService().inlayHint(new InlayHintParams(td, LspUtil.newRange(0, 0, 5, 0)));
		assertThat(request)
			.succeedsWithin(1, TimeUnit.SECONDS)
			.satisfies(
				hints -> assertThat(hints).isEmpty()
			);
	}

	@Test
	void inlayHintsShouldBeShownForNewLinesInInputStatements()
	{
		var module = createOrSaveFile("LIBONE", "SUB.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE

			INPUT (AD=MI)
				'Field: ' #FIELD /
				'Field2: ' #FIELD2 /
				'Bye'
			END
			""");

		var request = getContext().documentService().inlayHint(new InlayHintParams(module, LspUtil.newRange(0, 0, 8, 0)));
		assertThat(request)
			.succeedsWithin(1, TimeUnit.SECONDS)
			.satisfies(
				hints -> assertThat(hints).hasSize(3),
				hints -> assertThat(hints.get(0).getLabel().getLeft()).isEqualTo("line 1"),
				hints -> assertThat(hints.get(1).getLabel().getLeft()).isEqualTo("line 2"),
				hints -> assertThat(hints.get(2).getLabel().getLeft()).isEqualTo("line 3")
			);
	}

	@Test
	void inlayHintsShouldShowNoHintsIfAnInputHasNoOperands()
	{
		var module = createOrSaveFile("LIBONE", "SUB.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE

			INPUT (AD=MI)
			END
			""");

		var request = getContext().documentService().inlayHint(new InlayHintParams(module, LspUtil.newRange(0, 0, 8, 0)));
		assertThat(request)
			.succeedsWithin(1, TimeUnit.SECONDS)
			.satisfies(
				hints -> assertThat(hints).hasSize(0)
			);
	}

	private static LspTestContext testContext;

	@BeforeAll
	static void setupProject(@LspProjectName("emptyproject") LspTestContext context)
	{
		testContext = context;
	}

	@Override
	protected LspTestContext getContext()
	{
		return testContext;
	}
}
