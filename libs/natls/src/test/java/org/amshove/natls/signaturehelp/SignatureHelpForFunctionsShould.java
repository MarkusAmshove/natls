package org.amshove.natls.signaturehelp;

import org.amshove.testhelpers.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@IntegrationTest
class SignatureHelpForFunctionsShould extends SignatureHelpTest
{
	@Test
	void haveTheCompleteSignatureAsLabel() throws ExecutionException, InterruptedException, TimeoutException
	{
		var help = getSignatureHelpForModuleCall("ISSTH(<${}$>)");
		var signature = help.getSignatures().get(0);
		assertThat(signature.getLabel()).isEqualTo("ISSTH (P-PARAM :(A10), USING THEPDA, P-OPTIONAL :(A) DYNAMIC OPTIONAL)");
	}

	@Test
	void haveTheFirstParameterActiveWhenCursorIsAfterModuleName() throws ExecutionException, InterruptedException, TimeoutException
	{
		var help = getSignatureHelpForModuleCall("ISSTH(<${}$>)");
		var signature = help.getSignatures().get(0);

		assertThat(signature.getActiveParameter()).isZero();
		assertThat(signature.getParameters().get(0).getLabel().getLeft()).isEqualTo("P-PARAM :(A10)");
	}

	@Test
	void provideSignatureInformationForFunctionCallsInIfConditions() throws ExecutionException, InterruptedException, TimeoutException
	{
		var help = getSignatureHelpForModuleCall("""
			IF NOT ISSTH(<${}$>)
			IGNORE
			END-IF
			""");
		assertThat(help).isNotNull();
	}

	@Test
	void provideSignatureInformationForFunctionCallsInWhenConditions() throws ExecutionException, InterruptedException, TimeoutException
	{
		var help = getSignatureHelpForModuleCall("""
			DECIDE ON FIRST CONDITION
			WHEN ISSTH(<${}$>)
			IGNORE
			WHEN NONE
			IGNORE
			END-DECIDE
			""");
		assertThat(help).isNotNull();
	}

	@Test
	void provideSignatureInformationForFunctionCallsInWhenConditionsWhenAParameterIsAlreadyPresent() throws ExecutionException, InterruptedException, TimeoutException
	{
		var help = getSignatureHelpForModuleCall("""
			DECIDE ON FIRST CONDITION
			WHEN ISSTH(<#VAR, ${}$>)
			IGNORE
			WHEN NONE
			IGNORE
			END-DECIDE
			""");
		assertThat(help).isNotNull();
	}

	@Test
	void haveTheSecondParameterActiveWhenCursorIsAfterFirstParameter() throws ExecutionException, InterruptedException, TimeoutException
	{
		var help = getSignatureHelpForModuleCall("ISSTH(<'ASD', ${}$>)");
		var signature = help.getSignatures().get(0);

		assertThat(signature.getActiveParameter()).isEqualTo(1);
		assertThat(signature.getParameters().get(0).getLabel().getLeft()).isEqualTo("P-PARAM :(A10)");
	}

	@Test
	void haveTheSecondParameterActiveWhenCursorIsOnTheSecondParameter() throws ExecutionException, InterruptedException, TimeoutException
	{
		var help = getSignatureHelpForModuleCall("ISSTH(<'ASD', 'L${}$iteral'>)");
		var signature = help.getSignatures().get(0);

		assertThat(signature.getActiveParameter()).isEqualTo(1);
		assertThat(signature.getParameters().get(0).getLabel().getLeft()).isEqualTo("P-PARAM :(A10)");
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
			PARAMETER 1 P-PARAM (A10)
			PARAMETER USING THEPDA
			PARAMETER 1 P-OPTIONAL (A) DYNAMIC OPTIONAL
			END-DEFINE

			END-FUNCTION
			END
			""";
	}
}
