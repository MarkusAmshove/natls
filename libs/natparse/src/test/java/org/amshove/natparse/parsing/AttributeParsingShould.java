package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.IAttributeListNode;
import org.amshove.natparse.natural.IAttributeNode;
import org.amshove.natparse.natural.IOperandAttributeNode;
import org.amshove.natparse.natural.IValueAttributeNode;
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
		var attribute = valueAttribute(attributeList.attributes().first());
		assertThat(attribute.kind()).isEqualTo(SyntaxKind.CD);
		assertThat(attribute.value()).isEqualTo("RE");
	}

	@Test
	void parseASingleAttributeFollowingAVariable()
	{
		var operands = parseOperands("#VAR (CD=RE)");
		var attributeList = assertNodeType(operands.last(), IAttributeListNode.class);
		assertThat(attributeList.attributes()).hasSize(1);
		var attribute = valueAttribute(attributeList.attributes().first());
		assertThat(attribute.kind()).isEqualTo(SyntaxKind.CD);
		assertThat(attribute.value()).isEqualTo("RE");
	}

	@Test
	void parseAnAttributeListWithMultipleValues()
	{
		var operands = parseOperands("#VAR (CD=RE AD=IO AL=20)");
		var attributeList = assertNodeType(operands.last(), IAttributeListNode.class);
		assertThat(attributeList.attributes()).hasSize(3);

		assertThat(attributeList.attributes().first().kind()).isEqualTo(SyntaxKind.CD);
		assertThat(valueAttribute(attributeList.attributes().first()).value()).isEqualTo("RE");

		assertThat(attributeList.attributes().get(1).kind()).isEqualTo(SyntaxKind.AD);
		assertThat(valueAttribute(attributeList.attributes().get(1)).value()).isEqualTo("IO");

		assertThat(attributeList.attributes().get(2).kind()).isEqualTo(SyntaxKind.AL);
		assertThat(valueAttribute(attributeList.attributes().get(2)).value()).isEqualTo("20");
	}

	@Test
	void parseAttributesWithOperands()
	{
		var operands = parseOperands("(CV=#CONTROL)");
		var attributeList = assertNodeType(operands.last(), IAttributeListNode.class);
		var operandAttribute = operandAttribute(attributeList.attributes().first());
		assertThat(operandAttribute.kind()).isEqualTo(SyntaxKind.CV);
		assertIsVariableReference(operandAttribute.operand(), "#CONTROL");
	}

	@Test
	void mixOperandAttributesWithValueAttributes()
	{
		var operands = parseOperands("#VAR(CD=RE CV=#CONTROL AD=IO)");
		var attributeList = assertNodeType(operands.last(), IAttributeListNode.class);
		assertThat(attributeList.attributes()).hasSize(3);

		assertThat(attributeList.attributes().first().kind()).isEqualTo(SyntaxKind.CD);
		assertThat(valueAttribute(attributeList.attributes().first()).value()).isEqualTo("RE");

		assertThat(attributeList.attributes().get(1).kind()).isEqualTo(SyntaxKind.CV);
		assertIsVariableReference(operandAttribute(attributeList.attributes().get(1)).operand(), "#CONTROL");

		assertThat(attributeList.attributes().get(2).kind()).isEqualTo(SyntaxKind.AD);
		assertThat(valueAttribute(attributeList.attributes().get(2)).value()).isEqualTo("IO");
	}

	private IValueAttributeNode valueAttribute(IAttributeNode attributeNode)
	{
		return assertNodeType(attributeNode, IValueAttributeNode.class);
	}

	private IOperandAttributeNode operandAttribute(IAttributeNode attributeNode)
	{
		return assertNodeType(attributeNode, IOperandAttributeNode.class);
	}
}
