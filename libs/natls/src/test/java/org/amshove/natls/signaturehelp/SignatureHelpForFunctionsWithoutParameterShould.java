package org.amshove.natls.signaturehelp;

import org.amshove.testhelpers.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@IntegrationTest
class SignatureHelpForFunctionsWithoutParameterShould extends SignatureHelpTest
{
	@Test
	void stillShowASignatureEvenIfNoParameterAreExpected() throws ExecutionException, InterruptedException, TimeoutException
	{
		var help = getSignatureHelpForModuleCall("ISSTH(<${}$>)");
		var signature = help.getSignatures().get(0);
		assertThat(signature.getLabel()).isEqualTo("ISSTH ()");
	}

	@Override
	protected String getCalledModuleFilename()
	{
		return "ISSTH.NS7";
	}

	@Override
	protected String getCalledModuleSource()
	{
		return """
			DEFINE FUNCTION ISSTH
			RETURNS (L)

			DEFINE DATA
			LOCAL
			END-DEFINE

			END-FUNCTION
			END
			""";
	}
}
