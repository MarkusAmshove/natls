package org.amshove.natls.definition;

import org.amshove.natls.testlifecycle.LanguageServerTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.amshove.natls.testlifecycle.SourceWithCursor;
import org.amshove.testhelpers.IntegrationTest;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@IntegrationTest
class DefinitionEndpointTests extends LanguageServerTest
{
	private static LspTestContext context;

	@Test
	void definitionShouldReturnTheLocationOfAnExternalSubroutine()
	{
		var externalIdentifier = createOrSaveFile("LIBONE", "EXT.NSS", """
						DEFINE DATA
						PARAMETER 1 P-PARAM (A10)
						END-DEFINE
						DEFINE SUBROUTINE EXTERNAL-SUBROUTINE
						IGNORE
						END-SUBROUTINE
						END
			""");

		var definitions = getDefinitions("""
						DEFINE DATA
						LOCAL
						END-DEFINE
						
						PERFORM EXTE${}$RNAL-SUBROUTINE 'ABC'
						END
			""");

		assertThat(definitions).hasSize(1);
		assertThat(definitions.get(0).getUri()).isEqualTo(externalIdentifier.getUri());
	}

	@Test
	void definitionShouldReturnTheLocationOfTheLocalSubroutineIfAnExternalWithSameNameExists()
	{
		createOrSaveFile("LIBONE", "EXTLOC.NSS", """
						DEFINE DATA
						PARAMETER 1 P-PARAM (A10)
						END-DEFINE
						DEFINE SUBROUTINE EXTERNAL-AND-LOCAL-SUBROUTINE
						IGNORE
						END-SUBROUTINE
						END
			""");

		var sourceWithCursor = SourceWithCursor.fromSourceWithCursor("""
						DEFINE DATA
						LOCAL
						END-DEFINE
						DEFINE SUBROUTINE EXTERNAL-AND-LOCAL-SUBROUTINE
						IGNORE
						END-SUBROUTINE
						
						PERFORM EXTERN${}$AL-AND-LOCAL-SUBROUTINE
						END
			""");

		var caller = createOrSaveFile("LIBONE", "CALLER.NSN", sourceWithCursor);
		var definitions = getDefinitions(caller, sourceWithCursor.toSinglePosition());

		assertThat(definitions).hasSize(1);
		assertThat(definitions.get(0).getUri()).isEqualTo(caller.getUri());
	}

	@Test
	void definitionShouldResolveCallnats()
	{
		var called = createOrSaveFile("LIBONE", "CALLED.NSN", """
						DEFINE DATA LOCAL
						END-DEFINE
											
						END
			""");

		var definitions = getDefinitions("""
			DEFINE DATA LOCAL
			END-DEFINE
			CALLNAT 'CA${}$LLED'
			END
			""");

		assertThat(definitions).hasSize(1);
		assertThat(definitions.get(0).getUri()).isEqualTo(called.getUri());
	}

	@Test
	void definitionShouldResolveFunctions()
	{
		var called = createOrSaveFile("LIBONE", "ISSTH.NS7", """
						DEFINE FUNCTION ISSTH
						RETURNS (L)
						DEFINE DATA LOCAL
						END-DEFINE
						END-FUNCTION
						END
			""");

		var definitions = getDefinitions("""
			DEFINE DATA LOCAL
			END-DEFINE
			IF NOT ISSTH(<${}$>)
			IGNORE
			END-IF
			END
			""");

		assertThat(definitions).hasSize(1);
		assertThat(definitions.get(0).getUri()).isEqualTo(called.getUri());
	}

	@Test
	void definitionShouldResolveLocalVariables()
	{
   		assertSingleDefinitionInSameModule("""
			DEFINE DATA LOCAL
			1 #VAR (A10)
			END-DEFINE
			WRITE #VA${}$R
			END
			""",
			1, 2);
	}

	@Test
	void definitionShouldResolveLocalSubroutines()
	{
		assertSingleDefinitionInSameModule("""
			DEFINE DATA LOCAL
			END-DEFINE
			PERFORM THE-S${}$UB
			DEFINE SUBROUTINE THE-SUB
			IGNORE
			END-SUBROUTINE
			END
			""",
			3, 18);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"PERFORM ANOTHER-EXTERNAL-SU${}$B",
		"CALLNAT 'NONEXI${}$ST' ",
		"NOE${}$X(<>)"
	})
	void definitionShouldReturnAnEmptyListIfNoDefinitionIsFound(String call)
	{
		assertNoDefinitions("""
			DEFINE DATA LOCAL
			END-DEFINE
			%s
			END
			""".formatted(call));
	}

	@Test
	void definitionShouldReturnAnEmptyListIfCalledOnAKeyword()
	{
		assertNoDefinitions("""
			DEF${}$INE DATA LOCAL
			END-DEFINE
			END
			""");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"CALLNAT 'CALLED' #VA${}$R",
		"PERFORM EXTERNAL-SUBROUTINE #V${}$AR",
		"ISSTH(<#${}$VAR>)"
	})
	void definitionsShouldReturnTheVariableDefinitionWhenInvokedFromAParameterPosition(String call)
	{
		assertSingleDefinitionInSameModule("""
			DEFINE DATA LOCAL
			1 #VAR (A10)
			END-DEFINE
			%s
			END
			""".formatted(call),
			1, 2);
	}

	@Override
	protected LspTestContext getContext()
	{
		return context;
	}

	@BeforeAll
	static void beforeAll(@LspProjectName("emptyproject") LspTestContext context)
	{
		DefinitionEndpointTests.context = context;
	}

	private void assertSingleDefinitionInSameModule(String sourceWithCursor, int definitionLine, int definitionStart)
	{
		var source = SourceWithCursor.fromSourceWithCursor(sourceWithCursor);

		var caller = createOrSaveFile("LIBONE", "SINGLEDEF.NSN", source);

		var definitions = getDefinitions(caller, source.toSinglePosition());

		assertThat(definitions).hasSize(1);
		assertThat(definitions.get(0).getUri()).isEqualTo(caller.getUri());
		assertThat(definitions.get(0).getRange().getStart().getLine())
			.as("Lines did not match")
			.isEqualTo(definitionLine);
		assertThat(definitions.get(0).getRange().getStart().getCharacter())
			.as("Start offset/character did not match")
			.isEqualTo(definitionStart);
	}

	private void assertNoDefinitions(String sourceWithCursor)
	{
		try
		{
			var source = SourceWithCursor.fromSourceWithCursor(sourceWithCursor);
			var identifier = createOrSaveFile("LIBONE", "CALLER.NSN", source);
			var position = source.toSinglePosition();
			var params = new DefinitionParams(identifier, position);
			var result = context.documentService().definition(params).get(5, TimeUnit.SECONDS);
			if(result != null)
			{
				assertThat(result.getLeft()).isEmpty();
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private List<? extends Location> getDefinitions(String callerSource)
	{
		var sourceWithCursor = SourceWithCursor.fromSourceWithCursor(callerSource);
		var identifier = createOrSaveFile("LIBONE", "CALLER.NSN", sourceWithCursor);
		var position = sourceWithCursor.toSinglePosition();
		return getDefinitions(identifier, position);
	}

	private List<? extends Location> getDefinitions(TextDocumentIdentifier identifier, Position position)
	{
		try
		{
			var params = new DefinitionParams(identifier, position);
			return context.documentService().definition(params).get(5, TimeUnit.SECONDS).getLeft();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}