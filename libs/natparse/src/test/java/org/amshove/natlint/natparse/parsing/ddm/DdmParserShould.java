package org.amshove.natlint.natparse.parsing.ddm;

import org.amshove.natlint.natparse.natural.DataDefinitionModule;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DdmParserShould
{
	@Test
	void parseTheMetadataLine()
	{
		DataDefinitionModule dataDefinitionModule = new DdmParser().parseDdm("DB: 000 FILE: 128 - MY-EXCITING-DDM DEFAULT SEQUENCE: BH");

		assertThat(dataDefinitionModule.name()).isEqualTo("MY-EXCITING-DDM");
		assertThat(dataDefinitionModule.fileNumber()).isEqualTo("128");
		assertThat(dataDefinitionModule.databaseNumber()).isEqualTo("000");
		assertThat(dataDefinitionModule.defaultSequence()).isEqualTo("BH");
	}
}
