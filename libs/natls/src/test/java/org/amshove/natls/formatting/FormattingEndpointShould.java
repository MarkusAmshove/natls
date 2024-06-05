package org.amshove.natls.formatting;

import org.amshove.natls.testlifecycle.EmptyProjectTest;
import org.amshove.natls.testlifecycle.TextEditApplier;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.FormattingOptions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class FormattingEndpointShould extends EmptyProjectTest
{
	@Test
	void translateToUpperCase()
	{
		assertFormattedSource("""
			write 'Hi' /* don't translate comments or string literals
			""", """
			WRITE 'Hi' /* don't translate comments or string literals
			""");
	}

	@Test
	void notFormatDdms()
	{
		assertFormattedSource(
			"MYDDM.NSD",
			"""
T L DB Name                              F Leng  S D Remark
			""",
			"""
T L DB Name                              F Leng  S D Remark
			"""
		);
	}

	protected void assertFormattedSource(String file, String previousSource, String expectedSource)
	{
		try
		{
			var docId = createOrSaveFile("LIBONE", file, previousSource);
			var edits = getContext()
				.documentService()
				.formatting(
					new DocumentFormattingParams(docId, new FormattingOptions())
				)
				.get(1, TimeUnit.MINUTES);
			var formattedSource = new TextEditApplier().applyAll(edits, previousSource);
			assertThat(formattedSource).isEqualToNormalizingNewlines(expectedSource);
		}
		catch (InterruptedException | TimeoutException | ExecutionException e)
		{
			throw new RuntimeException(e);
		}
	}

	protected void assertFormattedSource(String previousSource, String expectedSource)
	{
		assertFormattedSource("SUB.NSN", previousSource, expectedSource);
	}
}
