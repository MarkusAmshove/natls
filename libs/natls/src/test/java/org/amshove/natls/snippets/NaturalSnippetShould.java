package org.amshove.natls.snippets;

import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTest;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@LspTest
public class NaturalSnippetShould
{
	private static LanguageServerFile testFile;
	private static LspTestContext testContext;

	@BeforeAll
	static void beforeAll(@LspProjectName("modrefparser") LspTestContext testContext)
	{
		testFile = testContext.project().findFileByReferableName("SUB");
		NaturalSnippetShould.testContext = testContext;
	}

	@ParameterizedTest
	@ValueSource(strings = { " mylabel", "my label", "mylabel " })
	void validateThatTheLabelDoesNotContainSpaces(String label)
	{
		assertThatThrownBy(() -> new NaturalSnippet(label))
			.isInstanceOf(SnippetInitializationException.class)
			.hasMessage("Can't initialize snippet: Label should not contain whitespace");
	}

	@Test
	void addTheLabelToTheCompletion()
	{
		var completion = new NaturalSnippet("mylabel")
			.insertsText("HI")
			.createCompletion(testFile);
		assertThat(completion.getLabel()).isEqualTo("mylabel");
	}

	@Test
	void havePlainTextFormatIfNoPlaceholdersAreGiven()
	{
		var completion = new NaturalSnippet("mylabel")
			.insertsText("HI")
			.createCompletion(testFile);
		assertThat(completion.getInsertTextFormat()).isEqualTo(InsertTextFormat.PlainText);
	}

	@Test
	void haveSnippetTextFormatIfNoPlaceholdersAreGiven()
	{
		var completion = new NaturalSnippet("mylabel")
			.insertsText("HI ${0}")
			.createCompletion(testFile);
		assertThat(completion.getInsertTextFormat()).isEqualTo(InsertTextFormat.Snippet);
	}

	@Test
	void haveTheCodeToBeInsertedAsDocumentation()
	{
		var completion = new NaturalSnippet("mylabel")
			.insertsText("WRITE 'Hello'")
			.createCompletion(testFile);
		assertThat(completion.getDocumentation().getRight().getValue()).isEqualTo(
			"""
				```natural
				WRITE 'Hello'
				```
				""".replace("\n", System.lineSeparator())
		);
	}

	@Test
	void addUsingsWhenNotAlreadyPresent()
	{
		var completion = new NaturalSnippet("mylabel")
			.insertsText("WRITE SNIPLDA")
			.needsUsing("SNIPLDA")
			.createCompletion(testFile);

		var usingInsert = completion.getAdditionalTextEdits().get(0);
		assertThat(usingInsert.getNewText()).isEqualTo("LOCAL USING SNIPLDA%n".formatted());
		assertThat(usingInsert.getRange().getStart().getLine()).isEqualTo(1);
		assertThat(usingInsert.getRange().getStart().getCharacter()).isEqualTo(0);
	}

	@Test
	void notAddAUsingIfItIsAlreadyPresent()
	{
		var completion = new NaturalSnippet("mylabel")
			.insertsText("WRITE SNIPLDA")
			.needsUsing("MYLDA")
			.createCompletion(testFile);

		assertThat(completion.getAdditionalTextEdits()).isNull();
	}

	@Test
	void addAUsingInFrontOfTheFirstVariableIfNoUsingIsPresent()
	{
		var completion = new NaturalSnippet("mylabel")
			.insertsText("WRITE SNIPLDA")
			.needsUsing("MYLDA")
			.createCompletion(testContext.project().findFileByReferableName("SUB2"));

		var insert = completion.getAdditionalTextEdits().get(0);
		assertThat(insert.getRange().getStart().getLine()).isEqualTo(1);
		assertThat(insert.getRange().getStart().getCharacter()).isEqualTo(0);
	}

	@Test
	void throwAnExceptionIfNoDefineDataIsPresent()
	{
		var snippet = new NaturalSnippet("mylabel")
			.insertsText("hi")
			.needsUsing("SOMELDA");

		assertThatThrownBy(()-> snippet.createCompletion(testContext.project().findFileByReferableName("PROG3")))
			.isInstanceOf(ResponseErrorException.class)
			.hasMessage("Can't complete snippet because no DEFINE DATA was found");
	}

	@Test
	void notBeApplicableIfFileTypesDontMatch()
	{
		var completion = new NaturalSnippet("mylabel")
			.insertsText("HI")
			.applicableInFiletypes(NaturalFileType.COPYCODE)
			.createCompletion(testFile);
		assertThat(completion).isNull();
	}

	@Test
	void notBeApplicableIfGivenPredicateDoesntMatch()
	{
		var completion = new NaturalSnippet("mylabel")
			.insertsText("HI")
			.applicableWhen(f -> f.getReferableName().startsWith("TC"))
			.createCompletion(testFile);
		assertThat(completion).isNull();
	}
}
