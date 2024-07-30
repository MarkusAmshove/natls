package org.amshove.natls;

import org.amshove.natls.languageserver.FindConstantsParams;
import org.amshove.natls.testlifecycle.EmptyProjectTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class FindConstantsEndpointShould extends EmptyProjectTest
{
	@Test
	void returnTheConstantsFromReachableLdas() throws ExecutionException, InterruptedException, TimeoutException
	{
		createOrSaveFile("LIBONE", "CONSTLDA.NSL", """
			DEFINE DATA LOCAL
			1 C-A (A1) CONST<'A'>
			1 C-B (A1) CONST<'B'>
			END-DEFINE
			""");

		var identifier = createOrSaveFile("LIBONE", "SUB.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			END
			""");

		var response = getContext().server().findConstants(new FindConstantsParams(identifier)).get(1, TimeUnit.MINUTES);
		assertThat(response.getConstants()).hasSize(2);

		var firstConst = response.getConstants().getFirst();
		assertThat(firstConst.name()).isEqualTo("C-A");
		assertThat(firstConst.source()).isEqualTo("CONSTLDA");

		var secondConst = response.getConstants().getLast();
		assertThat(secondConst.name()).isEqualTo("C-B");
		assertThat(secondConst.source()).isEqualTo("CONSTLDA");
	}
}
