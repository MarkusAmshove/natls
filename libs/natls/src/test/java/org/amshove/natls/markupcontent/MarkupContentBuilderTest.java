package org.amshove.natls.markupcontent;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public abstract class MarkupContentBuilderTest
{
	protected abstract String getExpectedKind();

	protected void assertContent(IMarkupContentBuilder builder, String expectedText)
	{
		assertThat(builder.build().getKind()).isEqualTo(getExpectedKind());
		assertThat(builder.build().getValue()).isEqualToNormalizingNewlines(expectedText);
	}
}
