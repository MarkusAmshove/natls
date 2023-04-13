package org.amshove.natls;

import org.amshove.natlint.linter.LinterContext;
import org.amshove.natls.languageserver.ReferableFileExistsParams;
import org.amshove.natls.testlifecycle.LanguageServerTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ReferableFileExistsEndpointShould extends LanguageServerTest
{
	private static LspTestContext testContext;

	@Override
	protected LspTestContext getContext()
	{
		return testContext;
	}

	@Test
	void returnTrueIfTheModuleCanBeFound(@LspProjectName("modrefparser") LspTestContext context) throws ExecutionException, InterruptedException
	{
		var response = context.server().referableFileExists(new ReferableFileExistsParams("LIBONE", "ISTRUE")).get();
		assertThat(response.getFileAlreadyExists()).isTrue();
	}

	@Test
	void returnTrueIfTheModuleCannotBeFound(@LspProjectName("modrefparser") LspTestContext context) throws ExecutionException, InterruptedException
	{
		var response = context.server().referableFileExists(new ReferableFileExistsParams("LIBTWO", "ISTRUE")).get();
		assertThat(response.getFileAlreadyExists()).isFalse();
	}

	@Test
	void returnTrueIfTheModuleLibraryDoesNotExist(@LspProjectName("modrefparser") LspTestContext context) throws ExecutionException, InterruptedException
	{
		var response = context.server().referableFileExists(new ReferableFileExistsParams("LIBTHREE", "ISTRUE")).get();
		assertThat(response.getFileAlreadyExists()).isFalse();
	}
}
