package org.amshove.natls.hovering;

import org.amshove.natls.hover.HoverProvider;
import org.amshove.natls.hover.MathFunctionHoverRegistry;
import org.amshove.natparse.natural.IMathFunctionOperandNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.reflections.Reflections;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class MathFunctionHoverShould extends HoveringTest
{
	@TestFactory
	Stream<DynamicTest> provideAHoverForAllKnownMathFunctions()
	{
		var reflections = new Reflections("org.amshove.natparse.parsing");
		var mathFunctions = reflections.getSubTypesOf(IMathFunctionOperandNode.class);
		var mathFunctionNodeClasses = mathFunctions.stream().filter(c -> !c.isInterface()).toList();
		return mathFunctionNodeClasses.stream()
			.filter(c -> !c.getSimpleName().equals("MathFunctionOperandNode"))
			.map(
				type -> dynamicTest(
					"%s should have a hover".formatted(type.getSimpleName()),
					() ->
					{
						var constructor = type.getDeclaredConstructor();
						constructor.setAccessible(true);
						assertThat(MathFunctionHoverRegistry.getHover(constructor.newInstance()))
							.isNotEqualTo(HoverProvider.EMPTY_HOVER);
					}
				)
			);
	}

	@Test
	void hoverAbsFunction()
	{
		assertHover(
			"""
			DEFINE DATA
			LOCAL
			1 #NUM1 (N10)
			1 #NUM2 (N10)
			END-DEFINE
			#NUM2 := AB${}$S(#NUM1)
			END
			""",
			"""
```ABS(parameter)```

Returns the absolute value of the given parameter.

Example:

```
ABS(-5)
```

returns 5.
"""
		);
	}

	@Test
	void hoverAtnFunction()
	{
		assertHover(
			"""
			DEFINE DATA
			LOCAL
			1 #NUM1 (N10)
			1 #NUM2 (N10)
			END-DEFINE
			#NUM2 := AT${}$N(#NUM1)
			END
			""",
			"""
```ATN(parameter)```

Returns the Arc tangent of the given parameter.
"""
		);
	}

	@Test
	void hoverCosFunction()
	{
		assertHover(
			"""
			DEFINE DATA
			LOCAL
			1 #NUM1 (N10)
			1 #NUM2 (N10)
			END-DEFINE
			#NUM2 := CO${}$S(#NUM1)
			END
			""",
			"""
```COS(parameter)```

Returns the Cosine of the given parameter.
"""
		);
	}

	@Test
	void hoverExpFunction()
	{
		assertHover(
			"""
			DEFINE DATA
			LOCAL
			1 #NUM1 (N10)
			1 #NUM2 (N10)
			END-DEFINE
			#NUM2 := EX${}$P(#NUM1)
			END
			""",
			"""
```EXP(parameter)```

Exponentiation of the given parameter to Euler's number `e`:

`e<sup>parameter</sup>`
"""
		);
	}

	@Test
	void hoverFracFunction()
	{
		assertHover(
			"""
			DEFINE DATA
			LOCAL
			1 #NUM1 (N10)
			1 #NUM2 (N10)
			END-DEFINE
			#NUM2 := FR${}$AC(#NUM1)
			END
			""",
			"""
```FRAC(parameter)```

Returns the fractional part of the given parameter.
"""
		);
	}

	@Test
	void hoverIntFunction()
	{
		assertHover(
			"""
			DEFINE DATA
			LOCAL
			1 #NUM1 (N10)
			1 #NUM2 (N10)
			END-DEFINE
			#NUM2 := IN${}$T(#NUM1)
			END
			""",
			"""
```INT(parameter)```

Returns the integer part of the given parameter.
"""
		);
	}

	@Test
	void hoverLogFunction()
	{
		assertHover(
			"""
			DEFINE DATA
			LOCAL
			1 #NUM1 (N10)
			1 #NUM2 (N10)
			END-DEFINE
			#NUM2 := LO${}$G(#NUM1)
			END
			""",
			"""
```LOG(parameter)```

Returns the natural logarithm of the given parameter.

A negative parameter value will be treated as positive.
"""
		);
	}

	@Test
	void hoverSgnFunction()
	{
		assertHover(
			"""
			DEFINE DATA
			LOCAL
			1 #NUM1 (N10)
			1 #NUM2 (N10)
			END-DEFINE
			#NUM2 := SG${}$N(#NUM1)
			END
			""",
			"""
```SGN(parameter)```

Returns the sign of the given parameter.

Returns:

- `-1` if the parameter is negative
- `0` if the parameter is zero
- `1` if the parameter is positive
"""
		);
	}

	@Test
	void hoverSinFunction()
	{
		assertHover(
			"""
			DEFINE DATA
			LOCAL
			1 #NUM1 (N10)
			1 #NUM2 (N10)
			END-DEFINE
			#NUM2 := SI${}$N(#NUM1)
			END
			""",
			"""
```SIN(parameter)```

Returns the Sine of the given parameter.
"""
		);
	}

	@Test
	void hoverSqrtFunction()
	{
		assertHover(
			"""
			DEFINE DATA
			LOCAL
			1 #NUM1 (N10)
			1 #NUM2 (N10)
			END-DEFINE
			#NUM2 := SQ${}$RT(#NUM1)
			END
			""",
			"""
```SQRT(parameter)```

Returns the square root of the given parameter.

A negative parameter value will be treated as positive.
"""
		);
	}

	@Test
	void hoverTanFunction()
	{
		assertHover(
			"""
			DEFINE DATA
			LOCAL
			1 #NUM1 (N10)
			1 #NUM2 (N10)
			END-DEFINE
			#NUM2 := TA${}$N(#NUM1)
			END
			""",
			"""
```TAN(parameter)```

Returns the Tangent of the given parameter.
"""
		);
	}

	@Test
	void hoverValFunction()
	{
		assertHover(
			"""
			DEFINE DATA
			LOCAL
			1 #NUM1 (N10)
			1 #TEXT (A10)
			END-DEFINE
			#NUM1 := VA${}$L(#TEXT)
			END
			""",
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
		);
	}

	@Test
	void hoverSumFunction()
	{
		assertHover(
			"""
			DEFINE DATA
			LOCAL
			1 MYVIEW VIEW OF MYDDM
			2 #NUM (N10)
			END-DEFINE
			READ MYVIEW BY DESC = 'A'
			#NUM := SU${}$M(#NUM)
			END-READ
			END
			""",
			"""
```SUM(parameter)```

Returns the sum of all values that were encountered in the processing loop where the call to `SUM` for the given field is located.

Successful `AT BREAK` conditions reset the sum accumulation.
"""
		);
	}

	@Test
	void hoverTotalFunction()
	{
		assertHover(
			"""
			DEFINE DATA
			LOCAL
			1 MYVIEW VIEW OF MYDDM
			2 #NUM (N10)
			END-DEFINE
			READ MYVIEW BY DESC = 'A'
			#NUM := TOT${}$AL(#NUM)
			END-READ
			END
			""",
			"""
```TOTAL(parameter)```

Returns the sum of all values that were encountered all processing loops where the call to `TOTAL` for the given field is located.

`AT BREAK` does *not* reset the accumulation (as opposed to `SUM` function).
"""
		);
	}

	@Test
	void hoverAverFunction()
	{
		assertHover(
			"""
			DEFINE DATA
			LOCAL
			1 MYVIEW VIEW OF MYDDM
			2 #NUM (N10)
			END-DEFINE
			READ MYVIEW BY DESC = 'A'
			#NUM := AV${}$ER(#NUM)
			END-READ
			END
			""",
			"""
```AVER(parameter)```

Returns the average of all values that were encountered in the processing loop where the call to `AVER` for the given field is located.
"""
		);
	}
}
