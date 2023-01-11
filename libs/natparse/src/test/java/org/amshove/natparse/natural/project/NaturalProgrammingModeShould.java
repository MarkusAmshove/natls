package org.amshove.natparse.natural.project;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class NaturalProgrammingModeShould
{

	@Test
	void returnProgrammingModeFromString()
	{
		var mode = NaturalProgrammingMode.fromString("S");
		assertThat(mode).isEqualTo(NaturalProgrammingMode.STRUCTURED);
		mode = NaturalProgrammingMode.fromString("R");
		assertThat(mode).isEqualTo(NaturalProgrammingMode.REPORTING);
		mode = NaturalProgrammingMode.fromString("");
		assertThat(mode).isEqualTo(NaturalProgrammingMode.UNKNOWN);
	}

}
