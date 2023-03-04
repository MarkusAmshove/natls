package org.amshove.natls.snippets;

import org.amshove.natls.project.LanguageServerFile;
import org.eclipse.lsp4j.CompletionItem;

import java.util.List;
import java.util.Objects;

public class StatementSnippetProvider implements ISnippetProvider
{
	private static final NaturalSnippet SOURCE_HEADER = new NaturalSnippet("sourceHeader")
		.insertsText("""
			* >Natural Source Header 000000
			* :Mode S
			* :CP
			* <Natural Source Header
			""");
	private static final NaturalSnippet SUBROUTINE = new NaturalSnippet("subr")
		.applicableWhen(f -> f.getType().canHaveBody())
		.insertsText("""
			/***********************************************************************
			DEFINE SUBROUTINE ${1:SUBROUTINE-NAME}
			/***********************************************************************
			
			${0:IGNORE}
			
			END-SUBROUTINE
			""");

	private static final NaturalSnippet IF = new NaturalSnippet("if")
		.applicableWhen(f -> f.getType().canHaveBody())
		.insertsText("""
			IF ${1:CONDITION}
			  ${0:IGNORE}
			END-IF
			""");

	private static final NaturalSnippet DECIDE_FOR_FIRST_CONDITION = new NaturalSnippet("decideForFirstCondition")
		.applicableWhen(f -> f.getType().canHaveBody())
		.insertsText("""
			DECIDE FOR FIRST CONDITION
			  WHEN ${1:CONDITION}
			    ${0:IGNORE}
			  WHEN NONE
				IGNORE /* TODO: Handling
			END-DECIDE
			""");

	private static final NaturalSnippet DECIDE_ON_FIRST_VALUE = new NaturalSnippet("decideOnFirstValue")
		.applicableWhen(f -> f.getType().canHaveBody())
		.insertsText("""
			DECIDE ON FIRST VALUE OF ${1:VALUE}
			  VALUE ${2:VALUE}
			    ${0:IGNORE}
			  NONE VALUE
				IGNORE /* TODO: Handling
			END-DECIDE
			""");

	private static final NaturalSnippet DECIDE_ON_EVERY_VALUE = new NaturalSnippet("decideOnEveryValue")
		.applicableWhen(f -> f.getType().canHaveBody())
		.insertsText("""
			DECIDE ON EVERY VALUE OF ${1:VALUE}
			  VALUE ${2:VALUE}
			    ${0:IGNORE}
			  NONE VALUE
				IGNORE /* TODO: Handling
			END-DECIDE
			""");

	private static final NaturalSnippet FOR = new NaturalSnippet("for")
		.applicableWhen(f -> f.getType().canHaveBody())
		.insertsText("""
			#S-${1:ARRAY} := *OCC(${1:ARRAY})
			FOR #I-${1} = 1 TO #S-${1}
			  ${0:IGNORE}
			END-FOR
			""");

	private static final NaturalSnippet RESIZE = new NaturalSnippet("resize")
		.applicableWhen(f -> f.getType().canHaveBody())
		.insertsText("""
			RESIZE ARRAY ${1:#ARRAY} TO (1:${2:1})
			""");

	private static final NaturalSnippet RESIZE_AND_RESET = new NaturalSnippet("resizeReset")
		.applicableWhen(f -> f.getType().canHaveBody())
		.insertsText("""
			RESIZE AND RESET ARRAY ${1:#ARRAY} TO (1:${2:1})
			""");

	private static final NaturalSnippet COMPRESS = new NaturalSnippet("compress")
		.applicableWhen(f -> f.getType().canHaveBody())
		.insertsText("""
			COMPRESS ${1:'Text'} INTO ${2:#VAR} ${3:LEAVING NO}
			""");

	private static final List<NaturalSnippet> snippets = List.of(
		SUBROUTINE,
		IF,
		DECIDE_FOR_FIRST_CONDITION,
		DECIDE_ON_FIRST_VALUE,
		DECIDE_ON_EVERY_VALUE,
		FOR,
		SOURCE_HEADER,
		RESIZE,
		RESIZE_AND_RESET,
		COMPRESS
	);

	@Override
	public List<CompletionItem> provideSnippets(LanguageServerFile file)
	{
		return snippets.stream()
			.map(s -> s.createCompletion(file))
			.filter(Objects::nonNull)
			.toList();
	}
}
