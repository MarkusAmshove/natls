package org.amshove.natls.hovering;

import org.junit.jupiter.api.Test;

class MaskHoverShould extends HoveringTest
{
	@Test
	void provideDocumentationWhenHoveringMask()
	{
		assertHover("""
			DEFINE DATA LOCAL
			1 #A (A10)
			END-DEFINE
			IF #A = M${}$ASK (AAA)
			  IGNORE
			END-IF
			END
			""", """
Tests if the value matches a mask/pattern

*Possible characters in MASK definitions:*


**. ? \\_**  These characters (dot, question mark, underscore) indicate a single position that will not be checked

**\\* %**  Both symbols indicate any number of positions that will not be checked

**/**  End of value (or the end before blanks), comparable to $ in regular expressions

**X**  Same character as the one at the same position of the operand after the mask definition

**A**  Alphabetical character, upper or lower case

**L**  Alphabetical character, lower case only (a-z)

**U**  Alphabetical character, upper case only (A-Z)

**'c'**  Literal value of **c**. Can multiple characters

**C**  Alphabetical character, numeric character or blank

**N**  Single digit

**n...**  Digit range `0-n`

**n1-n2 n1:n2**  Numeric range `n1-n2`

**DD**  Valid day number, 01-31, dependent on the values of MM and YY if specified

**MM**  Valid month number, 01-12

**YY**  Valid year, 00-99

**YYYY**  Valid year, 0000-2699

**JJJ**  Julian day, 001-366, dependent on YY or YYYY

**P**  Displayable character (U, L, N or S)**S**  Special character according to the active character set**H**  Hexadecimal content, A-F and 0-9

**Z**  Character whose left half-byte or whose right half-byte is hexadecimal""");
	}
}
