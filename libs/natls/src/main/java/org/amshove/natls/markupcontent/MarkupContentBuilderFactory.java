package org.amshove.natls.markupcontent;

import java.util.function.Supplier;

public class MarkupContentBuilderFactory
{
	private static Supplier<IMarkupContentBuilder> factory = () -> new PlaintextContentBuilder();

	public static IMarkupContentBuilder newBuilder()
	{
		return factory.get();
	}

	public static void configureFactory(Supplier<IMarkupContentBuilder> factory)
	{
		MarkupContentBuilderFactory.factory = factory;
	}
}
