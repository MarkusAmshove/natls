package org.amshove.natls.languageserver.inputstructure;

import org.amshove.natls.viewer.InputStructure;

import java.util.ArrayList;
import java.util.List;

public class InputStructureResponse
{
	private List<InputResponseElement> elements;

	private InputStructureResponse()
	{

	}

	public static InputStructureResponse fromInputStructure(InputStructure structure)
	{
		if (structure == null)
		{
			return null;
		}

		var response = new InputStructureResponse();
		response.elements = new ArrayList<>();
		int elementId = 1;
		for (var operand : structure.getOperands())
		{
			var responseElement = InputResponseElement.fromOutputElement(operand, structure.statementAttributes());
			responseElement.setId(elementId++);
			response.elements.add(responseElement);
		}
		return response;
	}

	public List<InputResponseElement> getElements()
	{
		return elements;
	}

	public void setElements(List<InputResponseElement> elements)
	{
		this.elements = elements;
	}
}
