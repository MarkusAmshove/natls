package org.amshove.natls;

import org.amshove.natparse.natural.project.NaturalFileType;
import org.eclipse.lsp4j.SymbolKind;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class SymbolKindsShould
{
	@TestFactory
	Stream<DynamicTest> haveAMappingForAllFileTypes()
	{
		return Stream.of(
			fileTypeTest("TCTEST", NaturalFileType.SUBPROGRAM, SymbolKind.Interface),
			fileTypeTest("SUB", NaturalFileType.SUBPROGRAM, SymbolKind.Class),
			fileTypeTest("SUBR", NaturalFileType.SUBROUTINE, SymbolKind.Method),
			fileTypeTest("HELP", NaturalFileType.HELPROUTINE, SymbolKind.Key),
			fileTypeTest("LDA", NaturalFileType.LDA, SymbolKind.Struct),
			fileTypeTest("GDA", NaturalFileType.GDA, SymbolKind.Struct),
			fileTypeTest("DDM", NaturalFileType.DDM, SymbolKind.Struct),
			fileTypeTest("PDA", NaturalFileType.PDA, SymbolKind.TypeParameter),
			fileTypeTest("MAP", NaturalFileType.MAP, SymbolKind.Enum),
			fileTypeTest("CCODE", NaturalFileType.COPYCODE, SymbolKind.Package),
			fileTypeTest("ISTRUE", NaturalFileType.FUNCTION, SymbolKind.Event)
		);
	}

	private DynamicTest fileTypeTest(String filename, NaturalFileType fileType, SymbolKind expectedKind)
	{
		return DynamicTest.dynamicTest("Map %s %s".formatted(fileType, filename), () -> assertThat(SymbolKinds.forFileType(filename, fileType)).isEqualTo(expectedKind));
	}
}
