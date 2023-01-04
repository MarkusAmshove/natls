package org.amshove.natls.signaturehelp;

import org.amshove.testhelpers.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@IntegrationTest
class SignatureHelpForCallnatToModuleWithoutParameterShould extends SignatureHelpTest
{
	@Test
	void haveTheCompleteSignatureAsLabel() throws ExecutionException, InterruptedException, TimeoutException
	{
		var help = getSignatureHelpForModuleCall("CALLNAT 'CALLED' ${}$");
		var signature = help.getSignatures().get(0);
		assertThat(signature.getLabel()).isEqualTo("CALLED ()");
	}

	@Override
	protected String getCalledModuleFilename()
	{
		return "CALLED.NSN";
	}

	@Override
	protected String getCalledModuleSource()
	{
		return """
			WRITE 'Hello, I have no parameter'
			END
			""";
	}
}
