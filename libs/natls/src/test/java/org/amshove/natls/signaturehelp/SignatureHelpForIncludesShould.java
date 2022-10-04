package org.amshove.natls.signaturehelp;

import org.amshove.testhelpers.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@IntegrationTest
class SignatureHelpForIncludesShould extends SignatureHelpTest
{
	@Test
	void stillShowASignatureEvenIfNoParameterAreExpected() throws ExecutionException, InterruptedException, TimeoutException
	{
		var help = getSignatureHelpForModuleCall("INCLUDE THECC${}$");
		var signature = help.getSignatures().get(0);
		assertThat(signature.getLabel()).isEqualTo("THECC (&1&, &2&, QUALIFIED.&3&)");
	}

	@Override
	protected String getCalledModuleFilename()
	{
		return "THECC.NSC";
	}

	@Override
	protected String getCalledModuleSource()
	{
		return """
			WRITE &1& &2& QUALIFIED.&3&
			""";
	}
}
