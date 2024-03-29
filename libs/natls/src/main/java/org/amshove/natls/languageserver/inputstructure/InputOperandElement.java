package org.amshove.natls.languageserver.inputstructure;

import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.output.IOutputOperandNode;

import java.util.ArrayList;
import java.util.List;

public class InputOperandElement extends InputResponseElement
{
	private String operand;
	private String type;
	private int length;
	private int sourceLine;
	private int sourceColumnStart;
	private int sourceColumnEnd;
	private final List<InputAttributeElement> attributes;

	protected InputOperandElement(IOutputOperandNode operand, ReadOnlyList<IAttributeNode> statementAttributes)
	{
		super(InputStructureElementKind.OPERAND);

		extractOperandValue(operand.operand());
		if (operand instanceof ICharacterRepetitionOperandNode repetition)
		{
			this.length = repetition.repetition();
			this.operand = this.getOperand().repeat(this.length);
		}

		this.attributes = new ArrayList<>();

		for (var statementAttribute : statementAttributes)
		{
			addAttribute(operand, statementAttribute);
		}

		for (var attribute : operand.attributes())
		{
			addAttribute(operand, attribute);
		}
	}

	private void addAttribute(IOutputOperandNode operand, IAttributeNode attributeNode)
	{
		if (!(attributeNode instanceof IValueAttributeNode valueAttributeNode))
		{
			return;
		}

		if (valueAttributeNode.kind() == SyntaxKind.AL)
		{
			this.length = Integer.parseInt(valueAttributeNode.value());
		}

		if (valueAttributeNode.kind() == SyntaxKind.NL)
		{
			var lengthValue = valueAttributeNode.value();
			var dataType = new DataType(DataFormat.NUMERIC, Double.parseDouble(lengthValue.replace(",", ".")));
			this.length = dataType.sumOfDigits() + 1; // + separator
			if (includesNumericSign(operand.attributes()))
			{
				this.length += 1;
			}
		}

		this.attributes.removeIf(a -> a.getKind().equalsIgnoreCase(attributeNode.kind().name()));
		this.attributes.add(new InputAttributeElement(attributeNode.kind().name(), valueAttributeNode.value()));
	}

	private boolean includesNumericSign(ReadOnlyList<IAttributeNode> attributes)
	{
		for (var attribute : attributes)
		{
			if (!(attribute instanceof IValueAttributeNode valueAttribute))
			{
				continue;
			}

			if (valueAttribute.kind() == SyntaxKind.SG)
			{
				return valueAttribute.value().equalsIgnoreCase("on");
			}
		}

		return false;
	}

	private void extractOperandValue(IOperandNode operand)
	{
		if (operand instanceof IVariableReferenceNode varRef)
		{
			this.sourceLine = varRef.referencingToken().line();
			this.sourceColumnStart = varRef.referencingToken().offsetInLine();
			this.sourceColumnEnd = varRef.referencingToken().endOffset();

			this.length = ((ITypedVariableNode) varRef.reference()).type().alphanumericLength();
			this.operand = varRef.referencingToken().symbolName();
			this.type = "reference";
			return;
		}

		if (operand instanceof ILiteralNode literal)
		{
			this.sourceLine = literal.position().line();
			this.sourceColumnStart = literal.position().offsetInLine();
			this.sourceColumnEnd = literal.position().endOffset();
			this.type = "literal";

			if (literal.token().kind() == SyntaxKind.STRING_LITERAL)
			{
				var stringValue = literal.token().stringValue();
				this.length = stringValue.length();
				this.operand = stringValue;
				return;
			}

			this.operand = literal.token().source();
			this.length = this.operand.length();
			return;
		}

		var token = ((ITokenNode) NodeUtil.deepFindLeaf(operand)).token();
		this.operand = token.source();
		this.type = "unknown";
		this.sourceLine = token.line();
		this.sourceColumnStart = token.offsetInLine();
		this.sourceColumnEnd = token.endOffset();
		this.length = this.operand.length();
	}

	public String getOperand()
	{
		return operand;
	}

	public List<InputAttributeElement> getAttributes()
	{
		return attributes;
	}

	public int getLength()
	{
		return length;
	}

	public int getSourceLine()
	{
		return sourceLine;
	}

	public int getSourceColumnStart()
	{
		return sourceColumnStart;
	}

	public int getSourceColumnEnd()
	{
		return sourceColumnEnd;
	}

	public String getType()
	{
		return type;
	}

	@Override
	public String toString()
	{
		return "%s { operand: %s, kind: %s }".formatted(getClass().getSimpleName(), getOperand(), getKind());
	}
}
