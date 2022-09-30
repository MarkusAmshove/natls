package org.amshove.natls.signaturehelp;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class SignatureHelpForExternalSubroutinesShould extends SignatureHelpTest
{
	@Test
	void haveTheCompleteSignatureAsLabel() throws ExecutionException, InterruptedException, TimeoutException
	{
		var help = getSignatureHelpForParameterList("PERFORM THESUB${}$");
		var signature = help.getSignatures().get(0);
		assertThat(signature.getLabel()).isEqualTo("THESUB (USING APDA, P-OPTIONAL :(A10) OPTIONAL, P-NUMBER :(N12))");
	}

	@Test
	void haveTheFirstParameterActiveWhenCursorIsAfterModuleName() throws ExecutionException, InterruptedException, TimeoutException
	{
		var help = getSignatureHelpForParameterList("PERFORM THESUB${}$");
		var signature = help.getSignatures().get(0);

		assertThat(help.getActiveParameter()).isEqualTo(1);
		assertThat(signature.getParameters().get(0).getLabel().getLeft()).isEqualTo("USING APDA");
	}

	@Test
	void haveTheSecondParameterActiveWhenCursorIsAfterFirstParameter() throws ExecutionException, InterruptedException, TimeoutException
	{
		var help = getSignatureHelpForParameterList("PERFORM THESUB APDA${}$");

		assertThat(help.getActiveParameter()).isEqualTo(1);
	}

	@Test
	void haveTheSecondParameterActiveWhenCursorIsInTheSecondParameter() throws ExecutionException, InterruptedException, TimeoutException
	{
		var help = getSignatureHelpForParameterList("PERFORM THESUB APDA 'Lit${}$eral'");

		assertThat(help.getActiveParameter()).isEqualTo(1);
	}

	@Override
	public String getCalledModuleFilename()
	{
		return "THESUB.NSS";
	}

	@Override
	public String getCalledModuleSource()
	{
		return """
			DEFINE DATA
			PARAMETER USING APDA
			PARAMETER
			1 P-OPTIONAL (A10) OPTIONAL
			1 P-NUMBER (N12)
			END-DEFINE

			DEFINE SUBROUTINE THESUB
				IGNORE
			END-SUBROUTINE

			END
			""";
	}
}
