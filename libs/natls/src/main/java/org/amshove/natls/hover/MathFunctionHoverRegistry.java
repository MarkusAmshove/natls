package org.amshove.natls.hover;

import java.util.HashMap;
import java.util.Map;

import org.amshove.natparse.natural.*;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;

public class MathFunctionHoverRegistry
{
	private static final Map<Class<? extends IMathFunctionOperandNode>, Hover> functionHovers;

	static
	{
		functionHovers = new HashMap<>();
		functionHovers.put(
			IAbsOperandNode.class, new Hover(
				markup(
					"""
```ABS(parameter)```

Returns the absolute value of the given parameter.

Example:

```
ABS(-5)
```

returns 5.
"""
				)
			)
		);
		functionHovers.put(
			IAtnOperandNode.class, new Hover(
				markup(
					"""
```ATN(parameter)```

Returns the Arc tangent of the given parameter.
"""
				)
			)
		);
		functionHovers.put(
			ICosOperandNode.class, new Hover(
				markup(
					"""
```COS(parameter)```

Returns the Cosine of the given parameter.
"""
				)
			)
		);
		functionHovers.put(
			IExpOperandNode.class, new Hover(
				markup(
					"""
```EXP(parameter)```

Exponentiation of the given parameter to Euler's number `e`:
"""
				)
			)
		);
		functionHovers.put(
			IFracOperandNode.class, new Hover(
				markup(
					"""
```FRAC(parameter)```

Returns the fractional part of the given parameter.

Example:

```
#NUM1 := 55,123
#NUM2 := FRAC(#NUM1) /* Results in 0,123
```
"""
				)
			)
		);
		functionHovers.put(
			IIntOperandNode.class, new Hover(
				markup(
					"""
```INT(parameter)```

Returns the integer part of the given parameter.

Example:

```
#NUM1 := 55,123
#NUM2 := INT(#NUM1) /* Results in 55,0
```
"""
				)
			)
		);
		functionHovers.put(
			ILogOperandNode.class, new Hover(
				markup(
					"""
```LOG(parameter)```

Returns the natural logarithm of the given parameter.

A negative parameter value will be treated as positive.
"""
				)
			)
		);
		functionHovers.put(
			ISignOperandNode.class, new Hover(
				markup(
					"""
```SGN(parameter)```

Returns the sign of the given parameter.

Returns:

- `-1` if the parameter is negative
- `0` if the parameter is zero
- `1` if the parameter is positive
"""
				)
			)
		);
		functionHovers.put(
			ISinOperandNode.class, new Hover(
				markup(
					"""
```SIN(parameter)```

Returns the Sine of the given parameter.
"""
				)
			)
		);
		functionHovers.put(
			ISqrtOperandNode.class, new Hover(
				markup(
					"""
```SQRT(parameter)```

Returns the square root of the given parameter.

A negative parameter value will be treated as positive.
"""
				)
			)
		);
		functionHovers.put(
			ITanOperandNode.class, new Hover(
				markup(
					"""
```TAN(parameter)```

Returns the Tangent of the given parameter.
"""
				)
			)
		);
		functionHovers.put(
			IValOperandNode.class, new Hover(
				markup(
					"""
```VAL(parameter)```

Converts the given alphanumeric parameter to a numeric value.
Leading and trailing blanks within `parameter` will be ignored.

Example:

```
#TEXT := '57'
#NUM := VAL(#TEXT) /* Contains the number 57
```
"""
				)
			)
		);
		functionHovers.put(
			ISumOperandNode.class, new Hover(
				markup(
					"""
```SUM(parameter)```

Returns the sum of all values that were encountered in the processing loop where the call to `SUM` for the given field is located.

Successful `AT BREAK` conditions reset the sum accumulation.
"""
				)
			)
		);
		functionHovers.put(
			ITotalOperandNode.class, new Hover(
				markup(
					"""
```TOTAL(parameter)```

Returns the sum of all values that were encountered all processing loops where the call to `TOTAL` for the given field is located.

`AT BREAK` does *not* reset the accumulation (as opposed to `SUM` function).
"""
				)
			)
		);
		functionHovers.put(
			IAverOperandNode.class, new Hover(
				markup(
					"""
```AVER(parameter)```

Returns the average of all values that were encountered in the processing loop where the call to `AVER` for the given field is located.
"""
				)
			)
		);

	}

	public static Hover getHover(IMathFunctionOperandNode mathFunction)
	{
		var interfaces = mathFunction.getClass().getInterfaces();
		for (var interface_ : interfaces)
		{
			if (functionHovers.containsKey(interface_))
			{
				return functionHovers.get(interface_);
			}
		}

		return HoverProvider.EMPTY_HOVER;
	}

	private static MarkupContent markup(String markup)
	{
		return new MarkupContent(MarkupKind.MARKDOWN, markup);
	}

	private MathFunctionHoverRegistry()
	{}
}
