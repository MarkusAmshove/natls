package org.amshove.natls;

import org.amshove.natls.languageserver.CalledModulesParams;
import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTest;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@LspTest
class OutgoingReferencesEndpointShould
{
	@Test
	void returnTheOutgoingReferencesOfAModule(@LspProjectName("modrefparser") LspTestContext context) throws ExecutionException, InterruptedException
	{
		var caller = context.project().findFileByReferableName("SUB");
		var callerIdentifier = new TextDocumentIdentifier(LspUtil.pathToUri(caller.getPath()));
		var response = context.server().calledModules(new CalledModulesParams(callerIdentifier)).get();
		assertThat(response.getUris()).hasSize(4);
	}

	@Test
	void returnAnEmptyListIfAModuleHasNoOutgoingReferences(@LspProjectName("modrefparser") LspTestContext context) throws ExecutionException, InterruptedException
	{
		var caller = context.project().findFileByReferableName("MY-EXTERNAL");
		var callerIdentifier = new TextDocumentIdentifier(LspUtil.pathToUri(caller.getPath()));
		var response = context.server().calledModules(new CalledModulesParams(callerIdentifier)).get();
		assertThat(response.getUris()).isEmpty();
	}
}
