package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ImplicitAttributeConversion
{
	private static final Map<String, SyntaxKind> IMPLICIT_CONVERSION;

	static
	{
		IMPLICIT_CONVERSION = new HashMap<>();
		IMPLICIT_CONVERSION.put("B", SyntaxKind.AD);
		IMPLICIT_CONVERSION.put("C", SyntaxKind.AD);
		IMPLICIT_CONVERSION.put("D", SyntaxKind.AD);
		IMPLICIT_CONVERSION.put("I", SyntaxKind.AD);
		IMPLICIT_CONVERSION.put("N", SyntaxKind.AD);
		IMPLICIT_CONVERSION.put("U", SyntaxKind.AD);
		IMPLICIT_CONVERSION.put("V", SyntaxKind.AD);

		IMPLICIT_CONVERSION.put("BL", SyntaxKind.CD);
		IMPLICIT_CONVERSION.put("GR", SyntaxKind.CD);
		IMPLICIT_CONVERSION.put("NE", SyntaxKind.CD);
		IMPLICIT_CONVERSION.put("PI", SyntaxKind.CD);
		IMPLICIT_CONVERSION.put("RE", SyntaxKind.CD);
		IMPLICIT_CONVERSION.put("TU", SyntaxKind.CD);
		IMPLICIT_CONVERSION.put("YE", SyntaxKind.CD);
	}

	private ImplicitAttributeConversion()
	{

	}

	@Nullable
	public static SyntaxKind getImplicitConversion(String source)
	{
		return IMPLICIT_CONVERSION.get(source.toUpperCase());
	}
}
