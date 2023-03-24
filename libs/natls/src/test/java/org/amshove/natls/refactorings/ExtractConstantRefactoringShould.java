package org.amshove.natls.refactorings;

import org.amshove.natls.codeactions.ICodeActionProvider;
import org.amshove.natls.testlifecycle.CodeActionTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ExtractConstantRefactoringShould extends CodeActionTest
{
	private static LspTestContext testContext;

	@ParameterizedTest
	@ValueSource(strings =
	{
		"'Hello'", "5", "2.1", "2,3", "TRUE", "FALSE"
	})
	void beApplicableWhenHoveringALiteral(String literal)
	{
		assertSingleCodeAction(
			"Extract constant",
			"LIBONE",
			"SUBN.NSN",
			"""
				    DEFINE DATA
				    LOCAL
				    END-DEFINE
				    WRITE %s${}$
				    END
				""".formatted(literal)
		);
	}

	@Test
	void extractAConstant()
	{
		var source = """
			DEFINE DATA
			LOCAL
			END-DEFINE
			WRITE 'He${}$llo'
			END
			""";
		assertSingleCodeAction(
			"Extract constant",
			"LIBONE",
			"SUBN.NSN",
			source
		)
			.insertsText(2, 0, "1 #C-NEW-CONSTANT (A5) CONST<'Hello'>%n".formatted())
			.insertsText(3, 6, "#C-NEW-CONSTANT");
	}

	@Test
	void extractAConstantAndReplaceAllLiteralsIfString()
	{
		var source = """
			DEFINE DATA
			LOCAL
			1 #NAME (A) DYNAMIC
			END-DEFINE
			WRITE 'He${}$llo'
			WRITE 'Hello'
			#NAME := 'Hello'
			END
			""";
		assertSingleCodeAction(
			"Extract constant",
			"LIBONE",
			"SUBN.NSN",
			source
		)
			.insertsText(2, 0, "1 #C-NEW-CONSTANT (A5) CONST<'Hello'>%n".formatted())
			.insertsText(4, 6, "#C-NEW-CONSTANT")
			.insertsText(5, 6, "#C-NEW-CONSTANT")
			.insertsText(6, 9, "#C-NEW-CONSTANT");
	}

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

	@Override
	protected ICodeActionProvider getCodeActionUnderTest()
	{
		return new ExtractConstantRefactoring();
	}
}
