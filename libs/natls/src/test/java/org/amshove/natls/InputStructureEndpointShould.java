package org.amshove.natls;

import org.amshove.natls.languageserver.inputstructure.InputOperandElement;
import org.amshove.natls.languageserver.inputstructure.InputStructureElementKind;
import org.amshove.natls.languageserver.inputstructure.InputStructureParams;
import org.amshove.natls.languageserver.inputstructure.InputStructureResponse;
import org.amshove.natls.testlifecycle.EmptyProjectTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class InputStructureEndpointShould extends EmptyProjectTest
{
	@Test
	void returnNullWhenNoInputFound()
	{
		assertThat(
			callEndpoint(
				"""
               DEFINE DATA PARAMETER
               END-DEFINE
               END
            """,
				0
			)
		).isNull();
	}

	@Test
	void returnNullWhenRequestingInputIndexThatDoesntExist()
	{
		assertThat(
			callEndpoint(
				"""
               DEFINE DATA PARAMETER
               END-DEFINE
               INPUT 'Hi'
               END
            """,
				1
			)
		).isNull();
	}

	@Test
	void returnAStructureWithLiteralElements()
	{
		var structure = callEndpoint(
			"""
				   DEFINE DATA PARAMETER
				   END-DEFINE
				   INPUT 'Hi'
				   END
				""",
			0
		);

		assertThat(structure.getElements().get(0).getKind()).isEqualTo(InputStructureElementKind.OPERAND);
		assertThat(((InputOperandElement) structure.getElements().get(0)).getType()).isEqualTo("literal");
	}

	@Test
	void returnAStructureWithVariableElements()
	{
		var structure = callEndpoint(
			"""
				   DEFINE DATA PARAMETER
				   1 #VAR (A10)
				   END-DEFINE
				   INPUT #VAR
				   END
				""",
			0
		);

		assertThat(structure.getElements().get(0).getKind()).isEqualTo(InputStructureElementKind.OPERAND);
		assertThat(((InputOperandElement) structure.getElements().get(0)).getType()).isEqualTo("reference");
	}

	private InputStructureResponse callEndpoint(String source, int inputIndex)
	{
		try
		{
			var document = createOrSaveFile("LIBONE", "MYMAP.NSM", source);

			var params = new InputStructureParams();
			params.setUri(document.getUri());
			params.setInputIndex(inputIndex);
			return getContext().server().inputStructure(params).get(5, TimeUnit.SECONDS);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
