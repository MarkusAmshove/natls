package org.amshove.natls;

import org.amshove.natls.languageserver.inputstructure.*;
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

	@Test
	void returnAStructureWithElementForTabulatorPositioning()
	{
		var structure = callEndpoint("""
				   DEFINE DATA PARAMETER
				   1 #VAR (A10)
				   END-DEFINE
				   INPUT 5T 'Hi'
				   END
			""", 0);

		assertThat(structure.getElements().get(0).getKind()).isEqualTo(InputStructureElementKind.COLUMN_POSITION);
		assertThat(((InputColumnPositionElement) structure.getElements().get(0)).getColumn()).isEqualTo(5);
	}

	@Test
	void returnAStructureWithElementForSpaceAdvancing()
	{
		var structure = callEndpoint("""
				   DEFINE DATA PARAMETER
				   1 #VAR (A10)
				   END-DEFINE
				   INPUT 5X 'Hi'
				   END
			""", 0);

		assertThat(structure.getElements().get(0).getKind()).isEqualTo(InputStructureElementKind.SPACES);
		assertThat(((InputSpaceElement) structure.getElements().get(0)).getSpaces()).isEqualTo(5);
	}

	@Test
	void returnAStructureWithElementForRepetition()
	{
		var structure = callEndpoint("""
				   DEFINE DATA PARAMETER
				   1 #VAR (A10)
				   END-DEFINE
				   INPUT '*' (5)
				   END
			""", 0);

		assertThat(structure.getElements().get(0).getKind()).isEqualTo(InputStructureElementKind.OPERAND);
		assertThat(((InputOperandElement) structure.getElements().get(0)).getLength()).isEqualTo(5);
	}

	@Test
	void returnAnElementWithAttributes()
	{
		var structure = callEndpoint("""
               DEFINE DATA PARAMETER
               END-DEFINE
               INPUT 'Hi' (AD=I)
               END
            """, 0);

		assertThat(structure.getElements().get(0).getKind()).isEqualTo(InputStructureElementKind.OPERAND);
		assertThat(((InputOperandElement) structure.getElements().get(0)).getAttributes()).anyMatch(a -> a.getKind().equalsIgnoreCase("AD") && a.getValue().equalsIgnoreCase("I"));
	}

	@Test
	void returnAnAdjustedLengthForAlphanumericsWithALAttribute()
	{
		var structure = callEndpoint("""
               DEFINE DATA PARAMETER
               1 #LONG (A10)
               END-DEFINE
               INPUT #LONG (AL=5)
               END
            """, 0);

		assertThat(structure.getElements().get(0).getKind()).isEqualTo(InputStructureElementKind.OPERAND);
		assertThat(((InputOperandElement) structure.getElements().get(0)).getAttributes()).anyMatch(a -> a.getKind().equalsIgnoreCase("AL") && a.getValue().equalsIgnoreCase("5"));
		assertThat(((InputOperandElement) structure.getElements().get(0)).getLength()).isEqualTo(5);
	}

	@Test
	void returnAnAdjustedLengthForNumericsWithNLAttribute()
	{
		var structure = callEndpoint("""
               DEFINE DATA PARAMETER
               1 #LONG (N10,2)
               END-DEFINE
               INPUT #LONG (NL=4,2)
               END
            """, 0);

		assertThat(structure.getElements().get(0).getKind()).isEqualTo(InputStructureElementKind.OPERAND);
		assertThat(((InputOperandElement) structure.getElements().get(0)).getAttributes()).anyMatch(a -> a.getKind().equalsIgnoreCase("NL") && a.getValue().equalsIgnoreCase("4,2"));
		assertThat(((InputOperandElement) structure.getElements().get(0)).getLength()).isEqualTo(7); // 4 in front, 2 after plus decimal separator
	}

	@Test
	void returnAnAdjustedLengthForNumericsWithNLAndSignOnAttribute()
	{
		var structure = callEndpoint("""
               DEFINE DATA PARAMETER
               1 #LONG (N10,2)
               END-DEFINE
               INPUT #LONG (NL=4,2 SG=ON)
               END
            """, 0);

		assertThat(structure.getElements().get(0).getKind()).isEqualTo(InputStructureElementKind.OPERAND);
		assertThat(((InputOperandElement) structure.getElements().get(0)).getAttributes()).anyMatch(a -> a.getKind().equalsIgnoreCase("NL") && a.getValue().equalsIgnoreCase("4,2"));
		assertThat(((InputOperandElement) structure.getElements().get(0)).getLength()).isEqualTo(8); // 4 in front, 2 after plus decimal separator, plus sign
	}

	@Test
	void returnAnAdjustedLengthForNumericsWithNLAndSignOffAttribute()
	{
		var structure = callEndpoint("""
               DEFINE DATA PARAMETER
               1 #LONG (N10,2)
               END-DEFINE
               INPUT #LONG (NL=4,2 SG=OFF)
               END
            """, 0);

		assertThat(structure.getElements().get(0).getKind()).isEqualTo(InputStructureElementKind.OPERAND);
		assertThat(((InputOperandElement) structure.getElements().get(0)).getAttributes()).anyMatch(a -> a.getKind().equalsIgnoreCase("NL") && a.getValue().equalsIgnoreCase("4,2"));
		assertThat(((InputOperandElement) structure.getElements().get(0)).getLength()).isEqualTo(7); // 4 in front, 2 after plus decimal separator
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
