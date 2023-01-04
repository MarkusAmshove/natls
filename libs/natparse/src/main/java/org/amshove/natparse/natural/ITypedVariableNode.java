package org.amshove.natparse.natural;

import java.util.stream.Collectors;

public interface ITypedVariableNode extends IVariableNode
{
	IVariableType type();

	default String formatTypeForDisplay()
	{
		var details = "";

		details += "(%s".formatted(type().format().identifier());
		if (type().length() > 0.0)
		{
			details += "%s".formatted(DataFormat.formatLength(type().length()));
		}

		if (isArray())
		{
			details += "/";
			details += dimensions().stream()
				.map(IArrayDimension::displayFormat)
				.collect(Collectors.joining(", "));
		}

		details += ")";

		if (type().hasDynamicLength())
		{
			details += " DYNAMIC";
		}

		return details;
	}
}
