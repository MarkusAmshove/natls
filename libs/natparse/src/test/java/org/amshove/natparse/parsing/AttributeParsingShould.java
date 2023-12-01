package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.IAttributeListNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class AttributeParsingShould extends AbstractOperandParsingTest
{
	@Test
	void parseASingleAttribute()
	{
		var operands = parseOperands("(CD=RE)");
		var attributeList = assertNodeType(operands.last(), IAttributeListNode.class);
		assertThat(attributeList.attributes()).hasSize(1);
		assertThat(attributeList.attributes().first().kind()).isEqualTo(SyntaxKind.CD);
		assertThat(attributeList.attributes().first().value()).isEqualTo("RE");
	}

	@Test
	void parseASingleAttributeFollowingAVariable()
	{
		var operands = parseOperands("#VAR (CD=RE)");
		var attributeList = assertNodeType(operands.last(), IAttributeListNode.class);
		assertThat(attributeList.attributes()).hasSize(1);
		assertThat(attributeList.attributes().first().kind()).isEqualTo(SyntaxKind.CD);
		assertThat(attributeList.attributes().first().value()).isEqualTo("RE");
	}

	@Test
	void parseAnAttributeListWithMultipleValues()
	{
		var operands = parseOperands("#VAR (CD=RE AD=IO AL=20)");
		var attributeList = assertNodeType(operands.last(), IAttributeListNode.class);
		assertThat(attributeList.attributes()).hasSize(3);

		assertThat(attributeList.attributes().first().kind()).isEqualTo(SyntaxKind.CD);
		assertThat(attributeList.attributes().first().value()).isEqualTo("RE");

		assertThat(attributeList.attributes().get(1).kind()).isEqualTo(SyntaxKind.AD);
		assertThat(attributeList.attributes().get(1).value()).isEqualTo("IO");

		assertThat(attributeList.attributes().get(2).kind()).isEqualTo(SyntaxKind.AL);
		assertThat(attributeList.attributes().get(2).value()).isEqualTo("20");
	}
}
