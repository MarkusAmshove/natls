package org.amshove.natls.testlifecycle;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class TextEditApplierShould
{
	private final TextEditApplier sut = new TextEditApplier();

	@Test
	void applyAnInsertEdit()
	{
		var source = """
			DEFINE DATA
			END-DEFINE

			#NAME := 'Tom'

			END
			""";
		var edit = new TextEdit();
		edit.setRange(new Range(new Position(1, 0), new Position(1, 0)));
		edit.setNewText("LOCAL\n1 #NAME (A) DYNAMIC\n");

		assertThat(sut.apply(edit, source))
			.isEqualTo("""
			DEFINE DATA
			LOCAL
			1 #NAME (A) DYNAMIC
			END-DEFINE

			#NAME := 'Tom'

			END
			""");
	}

	@Test
	void applyAnInlineInsertEdit()
	{
		var source = """
			DEFINE DATA
			END-DEFINE

			#NAME := 'Tom'

			END
			""";
		var edit = new TextEdit();
		edit.setRange(new Range(new Position(3, 1), new Position(3, 1)));
		edit.setNewText("SUR-");

		assertThat(sut.apply(edit, source))
			.isEqualTo("""
			DEFINE DATA
			END-DEFINE

			#SUR-NAME := 'Tom'

			END
			""");
	}

	@Test
	void applyAReplaceInlineEdit()
	{
		var source = """
			DEFINE DATA
			END-DEFINE

			GE

			END
			""";
		var edit = new TextEdit();
		edit.setRange(new Range(new Position(3, 0), new Position(3, 2)));
		edit.setNewText(">=");

		assertThat(sut.apply(edit, source))
			.isEqualTo("""
			DEFINE DATA
			END-DEFINE

			>=

			END
			""");
	}

	@Test
	void applyADeleteEditOverMultipleLines()
	{
		var source = """
			DEFINE DATA
			END-DEFINE

			#NAME := 'Tom'

			END
			""";
		var edit = new TextEdit();
		edit.setRange(new Range(new Position(2, 0), new Position(4, 0)));
		edit.setNewText("");

		assertThat(sut.apply(edit, source))
			.isEqualTo("""
			DEFINE DATA
			END-DEFINE

			END
			""");
	}
}
