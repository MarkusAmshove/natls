package org.amshove.natls;

import org.amshove.natls.testlifecycle.EmptyProjectTest;
import org.amshove.natparse.IPosition;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.*;
import org.junit.jupiter.api.Test;

import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class SourceExtractorShould extends EmptyProjectTest
{
	@Test
	void extractTheSourceOfANode()
	{
		createOrSaveFile("LIBONE", "SUB.NSN", """
   			DEFINE DATA LOCAL
   			1 #VAR (A10)
   			END-DEFINE
   			#VAR := 'Hi'
   			END
   			""");
		var file = getContext().languageService().findNaturalFile("LIBONE", "SUB");
		var module = file.module();
		var varReference = ((IAssignmentStatementNode) ((ISubprogram) module).body().statements().first()).target();
		assertThat(SourceExtractor.extractSource(varReference))
			.isEqualTo("#VAR");
	}

	@Test
	void extractTheSourceOfAFullStatement()
	{
		createOrSaveFile("LIBONE", "SUB.NSN", "CALLNAT 'MODULE'");
		var file = getContext().languageService().findNaturalFile("LIBONE", "SUB");
		var module = file.module();
		assertThat(SourceExtractor.extractSource(((IModuleWithBody) module).body().statements().first()))
			.isEqualTo("CALLNAT 'MODULE'");
	}

	@Test
	void extractAWholeLine()
	{
		createOrSaveFile("LIBONE", "SUB.NSN", """
            DEFINE DATA LOCAL
            1 #VAR (L)
            END-DEFINE
                 WRITE #VAR
            END
            """);
		var file = getContext().languageService().findNaturalFile("LIBONE", "SUB");
		assertThat(SourceExtractor.extractLine(((IModuleWithBody) file.module()).body().statements().first().position()))
			.isEqualTo("     WRITE #VAR");
	}

	@Test
	void throwAnExceptionWhenTheFileCantBeRead()
	{
		assertThatThrownBy(() -> SourceExtractor.extractSource(new StubNode(new StubPosition(Path.of("does", "not", "exist.txt")))))
			.isInstanceOf(UncheckedIOException.class);

	}

	@Test
	void throwAnExceptionWhenTheFileCantBeReadOnReadLines()
	{
		assertThatThrownBy(() -> SourceExtractor.extractLine(new StubPosition(Path.of("does", "not", "exist.txt"))))
			.isInstanceOf(UncheckedIOException.class);

	}

	private record StubPosition(Path filePath) implements IPosition
	{

		@Override
		public int offset()
		{
			return 0;
		}

		@Override
		public int offsetInLine()
		{
			return 0;
		}

		@Override
		public int line()
		{
			return 0;
		}

		@Override
		public int length()
		{
			return 0;
		}
	}

	private record StubNode(IPosition position) implements ISyntaxNode
	{
		@Override
		public ISyntaxNode parent()
		{
			return null;
		}

		@Override
		public IPosition diagnosticPosition()
		{
			return null;
		}

		@Override
		public boolean isInFile(Path path)
		{
			return false;
		}

		@Override
		public void destroy()
		{

		}

		@Override
		public ReadOnlyList<? extends ISyntaxNode> descendants()
		{
			return ReadOnlyList.empty();
		}

		@Override
		public void acceptNodeVisitor(ISyntaxNodeVisitor visitor)
		{}

		@Override
		public void acceptStatementVisitor(IStatementVisitor visitor)
		{}

		@Override
		public Iterator<ISyntaxNode> iterator()
		{
			return Collections.emptyIterator();
		}
	}
}
