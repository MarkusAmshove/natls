package org.amshove.natls;

import org.amshove.natls.languageserver.inputstructure.*;
import org.amshove.natls.testlifecycle.EmptyProjectTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

		var operand = assertHasOperand(structure, "Hi");
		assertThat(operand.getType()).isEqualTo("literal");
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

		var operand = assertHasOperand(structure, "#VAR");
		assertThat(operand.getType()).isEqualTo("reference");
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

		assertHasKind(structure, InputStructureElementKind.COLUMN_POSITION);
		assertThat(structure.getElements().getFirst().getKind()).isEqualTo(InputStructureElementKind.COLUMN_POSITION);
		assertThat(((InputColumnPositionElement) structure.getElements().getFirst()).getColumn()).isEqualTo(5);
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

		assertHasKind(structure, InputStructureElementKind.SPACES);
		assertThat(structure.getElements().getFirst().getKind()).isEqualTo(InputStructureElementKind.SPACES);
		assertThat(((InputSpaceElement) structure.getElements().getFirst()).getSpaces()).isEqualTo(5);
	}

	@Test
	void returnAStructureWithElementForNewLine()
	{
		var structure = callEndpoint("""
				   DEFINE DATA PARAMETER
				   1 #VAR (A10)
				   END-DEFINE
				   INPUT 'Hi' / 'Ho'
				   END
			""", 0);

		assertHasKind(structure, InputStructureElementKind.NEW_LINE);
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

		var operand = assertHasOperand(structure, "*****");
		assertThat(operand.getLength()).isEqualTo(5);
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

		assertHasOperand(structure, "Hi", withAttribute("AD", "I"));
	}

	@Test
	void inheritAttributesFromStatementLevel()
	{
		var structure = callEndpoint("""
               DEFINE DATA PARAMETER
               1 #VAR (A5)
               END-DEFINE
               INPUT (AD=I) 'Hi' #VAR
               END
            """, 0);

		assertHasOperand(structure, "Hi", withAttribute("AD", "I"));
		assertHasOperand(structure, "#VAR", withAttribute("AD", "I"));
	}

	@Test
	void inheritAttributesFromStatementLevelAndAddOwnAttributes()
	{
		var structure = callEndpoint("""
               DEFINE DATA PARAMETER
               1 #VAR (A5)
               END-DEFINE
               INPUT (AD=I) 'Hi' #VAR (AL=5)
               END
            """, 0);

		assertHasOperand(structure, "Hi", withAttribute("AD", "I"));
		assertHasOperand(structure, "#VAR", withAttribute("AD", "I"), withAttribute("AL", "5"));
	}

	@Test
	void overrideStatementLevelAttributes()
	{
		var structure = callEndpoint("""
               DEFINE DATA PARAMETER
               1 #VAR (A5)
               END-DEFINE
               INPUT (AD=I AL=5) 'Hi' #VAR (AL=10 AD=I)
               END
            """, 0);

		assertHasOperand(structure, "Hi", withAttribute("AL", "5"), withAttribute("AD", "I"));

		var operand = assertHasOperand(structure, "#VAR", withAttribute("AL", "10"), withAttribute("AD", "I"));
		assertThat(operand.getAttributes())
			.as("Operand should only have two attribute because of overrides, but got more")
			.hasSize(2);
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

		var operand = assertHasOperand(structure, "#LONG");
		assertThat(operand.getLength()).isEqualTo(5);
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

		var operand = assertHasOperand(structure, "#LONG", withAttribute("NL", "4,2"));
		assertThat(operand.getLength()).isEqualTo(7); // 4 in front, 2 after plus decimal separator
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

		var operand = assertHasOperand(structure, "#LONG", withAttribute("NL", "4,2"), withAttribute("SG", "ON"));
		assertThat(operand.getLength()).isEqualTo(8); // 4 in front, 2 after plus decimal separator, plus sign
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

		var operand = assertHasOperand(structure, "#LONG", withAttribute("NL", "4,2"), withAttribute("SG", "OFF"));
		assertThat(operand.getLength()).isEqualTo(7); // 4 in front, 2 after plus decimal separator
	}

	private InputOperandElement assertHasOperand(InputStructureResponse structure, String value, AttributeAssertion... attributeAssertions)
	{
		InputOperandElement operand = null;
		for (var element : structure.getElements().stream().filter(InputOperandElement.class::isInstance).map(InputOperandElement.class::cast).toList())
		{
			if (element.getKind().equalsIgnoreCase(InputStructureElementKind.OPERAND) && element.getOperand().equals(value))
			{
				operand = element;
				break;
			}
		}

		assertThat(operand)
			.as("Operand with value <%s> and kind <%s> not found in%n%s", value, InputStructureElementKind.OPERAND, formatElements(structure))
			.isNotNull();

		assertAttributes(operand, attributeAssertions);

		return operand;
	}

	private void assertAttributes(InputOperandElement operand, AttributeAssertion[] attributeAssertions)
	{
		for (var attributeAssertion : attributeAssertions)
		{
			assertThat(operand.getAttributes())
				.as(
					"Attribute with kind <%s> and value <%s> not found in%n%s", attributeAssertion.kind, attributeAssertion.value, operand.getAttributes().stream().map(InputAttributeElement::toString).collect(
						Collectors.joining("\n")
					)
				)
				.anyMatch(a -> a.getValue().equals(attributeAssertion.value) && a.getKind().equals(attributeAssertion.kind));
		}
	}

	private void assertHasKind(InputStructureResponse structure, String kind)
	{
		assertThat(structure.getElements())
			.as("Element with kind <%s> not found in %n%s", kind, formatElements(structure))
			.anyMatch(e -> e.getKind().equals(kind));
	}

	private AttributeAssertion withAttribute(String name, String value)
	{
		return new AttributeAssertion(name, value);
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

	private String formatElements(InputStructureResponse structure)
	{
		return structure.getElements().stream().map(e -> "%s -> %s".formatted(e.getKind(), e)).collect(
			Collectors.joining("\n")
		);
	}

	record AttributeAssertion(String kind, String value)
	{}
}
